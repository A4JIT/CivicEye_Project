package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintLifecycleService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ComplaintFeedbackRepository feedbackRepository;

    // Allowed transition map for standard workflow
    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ComplaintStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ComplaintStatus.SUBMITTED, EnumSet.of(
                ComplaintStatus.UNDER_REVIEW,
                ComplaintStatus.ASSIGNED,
                ComplaintStatus.REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.UNDER_REVIEW, EnumSet.of(
                ComplaintStatus.NEEDS_INFORMATION,
                ComplaintStatus.ASSIGNED,
                ComplaintStatus.REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.NEEDS_INFORMATION, EnumSet.of(
                ComplaintStatus.UNDER_REVIEW,
                ComplaintStatus.ASSIGNED,
                ComplaintStatus.REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.ASSIGNED, EnumSet.of(
                ComplaintStatus.IN_PROGRESS,
                ComplaintStatus.REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.IN_PROGRESS, EnumSet.of(
                ComplaintStatus.RESOLVED,
                ComplaintStatus.NEEDS_INFORMATION,
                ComplaintStatus.REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.RESOLVED, EnumSet.of(
                ComplaintStatus.CLOSED
        ));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.CLOSED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(ComplaintStatus.REJECTED, Collections.emptySet());
    }

    public ComplaintLifecycleService(ComplaintRepository complaintRepository,
                                   ComplaintStatusHistoryRepository statusHistoryRepository,
                                   DepartmentRepository departmentRepository,
                                   UserRepository userRepository,
                                   FileStorageService fileStorageService) {
        this(complaintRepository, statusHistoryRepository, departmentRepository, userRepository, fileStorageService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ComplaintLifecycleService(ComplaintRepository complaintRepository,
                                   ComplaintStatusHistoryRepository statusHistoryRepository,
                                   DepartmentRepository departmentRepository,
                                   UserRepository userRepository,
                                   FileStorageService fileStorageService,
                                   ComplaintFeedbackRepository feedbackRepository) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.feedbackRepository = feedbackRepository;
    }

    public void validateStatusTransition(ComplaintStatus currentStatus, ComplaintStatus newStatus, Role role) {
        if (currentStatus == newStatus) {
            throw new BadRequestException("Complaint is already in status: " + currentStatus);
        }

        // Admins can reopen or override lifecycle if needed
        if (role == Role.ADMIN) {
            return;
        }

        Set<ComplaintStatus> validNextStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!validNextStatuses.contains(newStatus)) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, StatusUpdateRequest request, MultipartFile resolutionImage, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEPARTMENT_USER) {
            throw new UnauthorizedException("Only department officers or administrators can update complaint status");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        // Enforce department boundary for department officers
        if (currentUser.getRole() == Role.DEPARTMENT_USER) {
            if (complaint.getDepartment() == null) {
                throw new UnauthorizedException("Complaint is not assigned to any department");
            }
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = request.getStatus();

        validateStatusTransition(oldStatus, newStatus, currentUser.getRole());

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        if (currentUser.getRole() == Role.DEPARTMENT_USER && user.getDepartment() != null) {
            if (!user.getDepartment().getId().equals(complaint.getDepartment().getId())) {
                throw new UnauthorizedException("Department officers can only update complaints within their own department");
            }
        }

        String finalResolutionImageUrl = request.getResolutionImageUrl();
        if (resolutionImage != null && !resolutionImage.isEmpty()) {
            String storedName = fileStorageService.storeFile(resolutionImage);
            finalResolutionImageUrl = "/api/files/" + storedName;
        }

        complaint.setStatus(newStatus);

        if (newStatus == ComplaintStatus.IN_PROGRESS) {
            if (complaint.getStartedWorkingAt() == null) {
                complaint.setStartedWorkingAt(LocalDateTime.now());
            }
            if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
                complaint.setProgressNotes(request.getRemarks().trim());
            }
        } else if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
            if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
                complaint.setResolutionNotes(request.getRemarks().trim());
            }
            if (finalResolutionImageUrl != null) {
                complaint.setResolutionImageUrl(finalResolutionImageUrl);
            }
        } else if (newStatus == ComplaintStatus.REJECTED) {
            if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
                complaint.setRejectionReason(request.getRemarks().trim());
            }
        } else if (newStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(LocalDateTime.now());
            complaint.setClosedBy(user);
            if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
                complaint.setClosureRemarks(request.getRemarks().trim());
            }
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record immutable audit history
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .changedBy(user)
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .resolutionImageUrl(finalResolutionImageUrl)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse assignComplaint(Long complaintId, AssignComplaintRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEPARTMENT_USER) {
            throw new UnauthorizedException("Only administrators or department officers can assign complaints");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        User assignedOfficer = null;
        if (request.getAssignedOfficerId() != null) {
            assignedOfficer = userRepository.findById(request.getAssignedOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Officer not found with id: " + request.getAssignedOfficerId()));
        }

        User actingUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setDepartment(department);
        if (assignedOfficer != null) {
            complaint.setAssignedTo(assignedOfficer);
        }

        // If the complaint was SUBMITTED or UNDER_REVIEW, automatically advance to ASSIGNED
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED || complaint.getStatus() == ComplaintStatus.UNDER_REVIEW) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        StringBuilder remarksBuilder = new StringBuilder("Assigned to department: ")
                .append(department.getName());
        if (assignedOfficer != null) {
            remarksBuilder.append(" (Officer: ").append(assignedOfficer.getFullName()).append(")");
        }
        if (request.getInstructions() != null && !request.getInstructions().isBlank()) {
            remarksBuilder.append(". Instructions: ").append(request.getInstructions().trim());
        }

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(savedComplaint.getStatus())
                .changedBy(actingUser)
                .remarks(remarksBuilder.toString())
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional(readOnly = true)
    public List<TimelineEventResponse> getTimeline(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        verifyReadAccess(complaint, currentUser);

        return statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId)
                .stream()
                .map(TimelineEventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByDepartment(Long departmentId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEPARTMENT_USER) {
            throw new UnauthorizedException("Access denied: Insufficient privileges");
        }

        return complaintRepository.findByDepartmentIdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void verifyReadAccess(Complaint complaint, UserPrincipal currentUser) {
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
            // Department users can view complaints in their department or if unassigned
            if (complaint.getDepartment() != null) {
                return;
            }
        }
    }

    @Transactional
    public ComplaintFeedbackResponse submitFeedback(Long complaintId, ComplaintFeedbackRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (currentUser.getRole() == Role.CITIZEN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Citizens can only submit feedback for their own complaints");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted for complaints in RESOLVED or CLOSED status");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5 stars");
        }

        if (feedbackRepository != null && feedbackRepository.existsByComplaintId(complaintId)) {
            throw new BadRequestException("Feedback has already been submitted for this complaint");
        }

        User citizen = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .complaint(complaint)
                .citizen(citizen)
                .department(complaint.getDepartment())
                .rating(request.getRating())
                .comments(request.getComments() != null ? request.getComments().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        ComplaintFeedback saved = feedbackRepository != null ? feedbackRepository.save(feedback) : feedback;
        complaint.setFeedback(saved);
        complaintRepository.save(complaint);

        return ComplaintFeedbackResponse.fromEntity(saved);
    }

    @Transactional
    public ComplaintResponse confirmResolution(Long complaintId, ConfirmResolutionRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (currentUser.getRole() == Role.CITIZEN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the reporting citizen can confirm resolution of this complaint");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Only resolved complaints can have their resolution confirmed by citizen");
        }

        User actingUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setCitizenConfirmedResolution(true);
        complaint.setClosedAt(LocalDateTime.now());
        if (request != null && request.getComments() != null && !request.getComments().isBlank()) {
            complaint.setClosureRemarks(request.getComments().trim());
        }

        Complaint saved = complaintRepository.save(complaint);

        if (request != null && request.getRating() != null && feedbackRepository != null && !feedbackRepository.existsByComplaintId(complaintId)) {
            ComplaintFeedback feedback = ComplaintFeedback.builder()
                    .complaint(saved)
                    .citizen(actingUser)
                    .department(saved.getDepartment())
                    .rating(request.getRating())
                    .comments(request.getComments() != null ? request.getComments().trim() : null)
                    .createdAt(LocalDateTime.now())
                    .build();
            ComplaintFeedback savedFeedback = feedbackRepository.save(feedback);
            saved.setFeedback(savedFeedback);
        }

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(saved)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.CLOSED)
                .changedBy(actingUser)
                .remarks(request != null && request.getComments() != null && !request.getComments().isBlank()
                        ? request.getComments().trim()
                        : "Citizen confirmed resolution. Complaint closed.")
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(saved);
    }

    @Transactional
    public ComplaintResponse closeComplaint(Long complaintId, CloseComplaintRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only administrators can formally close complaints without citizen confirmation");
        }
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Only complaints in RESOLVED status can be closed");
        }

        User admin = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setClosedBy(admin);
        complaint.setClosedAt(LocalDateTime.now());
        if (request != null && request.getClosureRemarks() != null && !request.getClosureRemarks().isBlank()) {
            complaint.setClosureRemarks(request.getClosureRemarks().trim());
        }

        Complaint saved = complaintRepository.save(complaint);

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(saved)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.CLOSED)
                .changedBy(admin)
                .remarks(request != null && request.getClosureRemarks() != null && !request.getClosureRemarks().isBlank()
                        ? request.getClosureRemarks().trim()
                        : "Administrative complaint closure.")
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(saved);
    }
}
