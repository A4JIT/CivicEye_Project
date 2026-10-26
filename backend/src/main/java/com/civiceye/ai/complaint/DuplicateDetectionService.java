package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.DuplicateCheckRequest;
import com.civiceye.ai.complaint.dto.DuplicateCheckResponse;
import com.civiceye.ai.complaint.dto.DuplicateMatchDto;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplicateDetectionService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetectionService.class);

    private final ComplaintRepository complaintRepository;
    private final ComplaintUpvoteRepository upvoteRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    public DuplicateDetectionService(ComplaintRepository complaintRepository,
                                     ComplaintUpvoteRepository upvoteRepository,
                                     ComplaintStatusHistoryRepository statusHistoryRepository,
                                     UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.upvoteRepository = upvoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
    }

    private static final double DUPLICATE_THRESHOLD = 0.58;
    private static final double PROXIMITY_RADIUS_METERS = 400.0;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "in", "on", "at", "to", "for", "of", "with",
            "is", "are", "was", "near", "by", "from", "road", "street", "lane", "area", "please",
            "very", "get", "got", "due", "this", "that", "there", "here"
    );

    /**
     * Check if a proposed complaint has existing active duplicates nearby.
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkDuplicates(DuplicateCheckRequest request) {
        if (request == null) {
            return DuplicateCheckResponse.builder()
                    .isDuplicate(false)
                    .highestScore(0.0)
                    .matches(Collections.emptyList())
                    .build();
        }

        ComplaintCategory category = null;
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            try {
                category = ComplaintCategory.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid category
            }
        }

        // Search across all active complaints so duplicates aren't missed due to slight category mismatch
        List<Complaint> candidates = complaintRepository.findAllActive();

        List<DuplicateMatchDto> matches = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");

        for (Complaint candidate : candidates) {
            List<String> reasons = new ArrayList<>();

            // 1. Category match
            double categoryScore = 0.0;
            if (category != null && category.equals(candidate.getCategory())) {
                categoryScore = 1.0;
                reasons.add("Same issue category (" + candidate.getCategory().name() + ")");
            } else if (category == null || candidate.getCategory() == null) {
                categoryScore = 0.4;
            }

            // 2. Spatial GPS or Location Text Proximity
            double locationScore = 0.0;
            Double distanceMeters = null;

            if (request.getLatitude() != null && request.getLongitude() != null
                    && candidate.getLatitude() != null && candidate.getLongitude() != null) {
                distanceMeters = calculateDistanceMeters(
                        request.getLatitude(), request.getLongitude(),
                        candidate.getLatitude(), candidate.getLongitude()
                );

                if (distanceMeters <= 35.0) {
                    locationScore = 1.0;
                    reasons.add("Exact spot match (" + Math.round(distanceMeters) + "m away)");
                } else if (distanceMeters <= 80.0) {
                    locationScore = 0.90;
                    reasons.add("Immediate vicinity (" + Math.round(distanceMeters) + "m away)");
                } else if (distanceMeters <= 150.0) {
                    locationScore = 0.75;
                    reasons.add("Nearby location (" + Math.round(distanceMeters) + "m away)");
                } else if (distanceMeters <= PROXIMITY_RADIUS_METERS) {
                    locationScore = 0.45;
                    reasons.add("Same neighborhood (" + Math.round(distanceMeters) + "m away)");
                }
            } else if (request.getLocation() != null && candidate.getLocation() != null) {
                double textLocScore = calculateTokenSimilarity(request.getLocation(), candidate.getLocation());
                if (textLocScore >= 0.35) {
                    locationScore = textLocScore;
                    reasons.add("Matching location landmarks");
                }
            }

            // 3. Text and Semantic Token Similarity with fuzzy spelling tolerance
            String newText = (request.getTitle() != null ? request.getTitle() : "") + " " +
                    (request.getDescription() != null ? request.getDescription() : "");
            String candidateText = (candidate.getTitle() != null ? candidate.getTitle() : "") + " " +
                    (candidate.getDescription() != null ? candidate.getDescription() : "");

            double textScore = calculateTokenSimilarity(newText, candidateText);
            if (textScore >= 0.30) {
                reasons.add("Matching issue description (" + Math.round(textScore * 100) + "% word overlap)");
            }

            // 4. Multi-Factor Composite Score
            double compositeScore;
            if (distanceMeters != null) {
                compositeScore = (categoryScore * 0.25) + (locationScore * 0.45) + (textScore * 0.30);
            } else {
                compositeScore = (categoryScore * 0.30) + (locationScore * 0.35) + (textScore * 0.35);
            }

            // High similarity condition: threshold met, or close distance with reasonable similarity
            boolean isCandidateDuplicate = compositeScore >= DUPLICATE_THRESHOLD
                    || (distanceMeters != null && distanceMeters <= 100.0 && compositeScore >= 0.48)
                    || (categoryScore >= 1.0 && textScore >= 0.55);

            if (isCandidateDuplicate) {
                double finalScore = Math.min(0.99, Math.max(compositeScore, 0.50));
                matches.add(DuplicateMatchDto.builder()
                        .complaintId(candidate.getId())
                        .complaintNumber(candidate.getComplaintNumber())
                        .title(candidate.getTitle())
                        .description(candidate.getDescription())
                        .category(candidate.getCategory() != null ? candidate.getCategory().name() : "")
                        .status(candidate.getStatus() != null ? candidate.getStatus().name() : "")
                        .priority(candidate.getPriority() != null ? candidate.getPriority().name() : "")
                        .location(candidate.getLocation())
                        .latitude(candidate.getLatitude())
                        .longitude(candidate.getLongitude())
                        .distanceMeters(distanceMeters != null ? Double.parseDouble(df.format(distanceMeters)) : null)
                        .similarityScore(Double.parseDouble(df.format(finalScore)))
                        .upvotesCount(candidate.getUpvotesCount() != null ? candidate.getUpvotesCount() : 1)
                        .createdAt(candidate.getCreatedAt())
                        .matchReasons(reasons)
                        .build());
            }
        }

        // Sort highest match first
        matches.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        boolean isDuplicate = !matches.isEmpty();
        DuplicateMatchDto primaryMatch = isDuplicate ? matches.get(0) : null;
        Double highestScore = isDuplicate ? primaryMatch.getSimilarityScore() : 0.0;

        String recommendationMessage = isDuplicate
                ? "Similar active issue already reported nearby (" + Math.round(highestScore * 100) + "% match). Upvoting this existing complaint increases municipal priority."
                : "No duplicate complaints detected nearby. Issue is unique.";

        return DuplicateCheckResponse.builder()
                .isDuplicate(isDuplicate)
                .highestScore(highestScore)
                .primaryMatch(primaryMatch)
                .topMatch(primaryMatch)
                .matches(matches)
                .recommendationMessage(recommendationMessage)
                .build();
    }

    /**
     * Upvote an existing complaint using UserPrincipal.
     */
    @Transactional
    public Map<String, Object> upvoteComplaint(Long complaintId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in to upvote a complaint.");
        }
        User citizen = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        return upvoteComplaint(complaintId, citizen);
    }

    /**
     * Upvote an existing complaint to increase municipal urgency without creating a duplicate.
     */
    @Transactional
    public Map<String, Object> upvoteComplaint(Long complaintId, User citizen) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        boolean alreadyUpvoted = upvoteRepository.existsByComplaintIdAndCitizenId(complaintId, citizen.getId());
        if (alreadyUpvoted) {
            return Map.of(
                    "success", true,
                    "alreadyUpvoted", true,
                    "hasUpvoted", true,
                    "message", "You have already upvoted this complaint.",
                    "complaintId", complaint.getId(),
                    "complaintNumber", complaint.getComplaintNumber(),
                    "upvotesCount", complaint.getUpvotesCount() != null ? complaint.getUpvotesCount() : 1
            );
        }

        ComplaintUpvote upvote = ComplaintUpvote.builder()
                .complaint(complaint)
                .citizen(citizen)
                .build();
        upvoteRepository.save(upvote);

        int currentCount = complaint.getUpvotesCount() != null ? complaint.getUpvotesCount() : 1;
        int newCount = currentCount + 1;
        complaint.setUpvotesCount(newCount);

        // Auto-escalate priority based on citizen endorsement threshold
        if (newCount >= 10 && complaint.getPriority() != Priority.CRITICAL) {
            complaint.setPriority(Priority.CRITICAL);
            log.info("Complaint {} auto-escalated to CRITICAL due to {} upvotes", complaint.getComplaintNumber(), newCount);
        } else if (newCount >= 5 && complaint.getPriority() == Priority.MEDIUM) {
            complaint.setPriority(Priority.HIGH);
            log.info("Complaint {} auto-escalated to HIGH due to {} upvotes", complaint.getComplaintNumber(), newCount);
        }

        complaintRepository.save(complaint);

        return Map.of(
                "success", true,
                "alreadyUpvoted", false,
                "hasUpvoted", true,
                "message", "Complaint successfully upvoted! Priority escalated based on citizen demand.",
                "complaintId", complaint.getId(),
                "complaintNumber", complaint.getComplaintNumber(),
                "upvotesCount", newCount,
                "priority", complaint.getPriority().name()
        );
    }

    /**
     * Admin action: Merge a duplicate complaint into a parent complaint using UserPrincipal.
     */
    @Transactional
    public Map<String, Object> mergeDuplicate(Long duplicateId, String parentIdentifier, UserPrincipal currentUser, String remarks) {
        if (currentUser == null) {
            throw new UnauthorizedException("You must be logged in as an administrator to merge complaints.");
        }
        User admin = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        return mergeDuplicate(duplicateId, parentIdentifier, admin, remarks);
    }

    @Transactional
    public Map<String, Object> mergeDuplicate(Long duplicateId, Long parentId, UserPrincipal currentUser, String remarks) {
        return mergeDuplicate(duplicateId, String.valueOf(parentId), currentUser, remarks);
    }

    @Transactional
    public Map<String, Object> mergeDuplicate(Long duplicateId, Long parentId, User admin, String remarks) {
        return mergeDuplicate(duplicateId, String.valueOf(parentId), admin, remarks);
    }

    /**
     * Admin action: Merge a duplicate complaint into a parent complaint by parent ID or Complaint Number.
     */
    @Transactional
    public Map<String, Object> mergeDuplicate(Long duplicateId, String parentIdentifier, User admin, String remarks) {
        Complaint duplicate = complaintRepository.findById(duplicateId)
                .orElseThrow(() -> new ResourceNotFoundException("Duplicate complaint not found with ID: " + duplicateId));

        Complaint parent = null;
        if (parentIdentifier != null) {
            String trimmed = parentIdentifier.trim();
            try {
                Long pId = Long.parseLong(trimmed);
                parent = complaintRepository.findById(pId).orElse(null);
            } catch (NumberFormatException ignored) {}

            if (parent == null) {
                parent = complaintRepository.findByComplaintNumber(trimmed).orElse(null);
            }
        }

        if (parent == null) {
            throw new ResourceNotFoundException("Parent complaint not found with ID or ticket number: " + parentIdentifier);
        }

        duplicate.setDuplicateOf(parent);
        duplicate.setStatus(ComplaintStatus.REJECTED);
        duplicate.setRejectionReason("Duplicate report merged into primary ticket #" + parent.getComplaintNumber() +
                (remarks != null && !remarks.isBlank() ? " • " + remarks : ""));

        // Transfer upvotes to parent
        int duplicateUpvotes = duplicate.getUpvotesCount() != null ? duplicate.getUpvotesCount() : 1;
        parent.setUpvotesCount((parent.getUpvotesCount() != null ? parent.getUpvotesCount() : 1) + duplicateUpvotes);

        complaintRepository.save(duplicate);
        complaintRepository.save(parent);

        // Audit entry
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(duplicate)
                .fromStatus(duplicate.getStatus())
                .toStatus(ComplaintStatus.REJECTED)
                .changedBy(admin)
                .remarks("Merged as duplicate into parent complaint #" + parent.getComplaintNumber())
                .build();
        statusHistoryRepository.save(history);

        return Map.of(
                "success", true,
                "message", "Complaint #" + duplicate.getComplaintNumber() + " successfully merged into #" + parent.getComplaintNumber(),
                "duplicateId", duplicate.getId(),
                "parentId", parent.getId(),
                "parentNumber", parent.getComplaintNumber(),
                "parentUpvotesCount", parent.getUpvotesCount()
        );
    }

    /**
     * Calculate Haversine distance in meters between two lat/long coordinates.
     */
    public static double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Calculate token similarity with phonetic and Levenshtein typo tolerance.
     */
    public static double calculateTokenSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        Set<String> tokens1 = tokenize(text1);
        Set<String> tokens2 = tokenize(text2);
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

        int matches = 0;
        for (String t1 : tokens1) {
            for (String t2 : tokens2) {
                if (t1.equals(t2) || isFuzzyMatch(t1, t2)) {
                    matches++;
                    break;
                }
            }
        }

        int totalUnique = tokens1.size() + tokens2.size() - matches;
        return totalUnique > 0 ? (double) matches / (double) totalUnique : 0.0;
    }

    private static boolean isFuzzyMatch(String s1, String s2) {
        if (Math.abs(s1.length() - s2.length()) > 2) return false;
        if (s1.length() >= 4 && s2.length() >= 4) {
            if (s1.startsWith(s2) || s2.startsWith(s1)) return true;
        }
        return levenshteinDistance(s1, s2) <= (Math.min(s1.length(), s2.length()) >= 5 ? 2 : 1);
    }

    private static int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private static Set<String> tokenize(String input) {
        return Arrays.stream(input.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() > 2 && !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }
}
