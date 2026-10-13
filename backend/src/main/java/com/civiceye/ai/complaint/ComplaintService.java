package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.CreateComplaintRequest;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.storage.FileStorageService;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            FileStorageService fileStorageService) {
        this(complaintRepository, userRepository, fileStorageService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            FileStorageService fileStorageService,
                            ComplaintStatusHistoryRepository statusHistoryRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public synchronized ComplaintResponse createComplaint(CreateComplaintRequest request, MultipartFile image, Long citizenId) {
        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + citizenId));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(image);
            imageUrl = "/api/files/" + storedFileName;
        }

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = Complaint.builder()
                .complaintNumber(complaintNumber)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .location(request.getLocation().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .imageUrl(imageUrl)
                .citizen(citizen)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        if (statusHistoryRepository != null) {
            ComplaintStatusHistory initialHistory = ComplaintStatusHistory.builder()
                    .complaint(savedComplaint)
                    .fromStatus(null)
                    .toStatus(ComplaintStatus.SUBMITTED)
                    .changedBy(citizen)
                    .remarks("Complaint registered by citizen.")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            statusHistoryRepository.save(initialHistory);
        }

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        verifyAccess(complaint, currentUser);
        return ComplaintResponse.fromEntity(complaint);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByNumber(String complaintNumber, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with number: " + complaintNumber));

        return ComplaintResponse.fromEntity(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(Long citizenId) {
        return complaintRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll()
                .stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public synchronized String generateComplaintNumber() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "CE-" + currentYear + "-";

        Optional<Complaint> latestComplaint = complaintRepository.findTopByComplaintNumberStartingWithOrderByIdDesc(prefix);

        int nextSequence = 1;
        if (latestComplaint.isPresent()) {
            String latestNumber = latestComplaint.get().getComplaintNumber();
            try {
                String sequencePart = latestNumber.substring(prefix.length());
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (Exception ignored) {
                nextSequence = 1;
            }
        }

        return String.format("%s%04d", prefix, nextSequence);
    }

    private void verifyAccess(Complaint complaint, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (currentUser.getRole() == Role.CITIZEN) {
            if (!complaint.getCitizen().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Access denied: You can only view your own complaints");
            }
            return;
        }

        if (currentUser.getRole() == Role.DEPARTMENT_USER) {
            if (complaint.getDepartment() == null) {
                throw new UnauthorizedException("Access denied: Complaint is not assigned to your department");
            }
            return;
        }

        throw new UnauthorizedException("Access denied: Insufficient privileges");
    }
}
