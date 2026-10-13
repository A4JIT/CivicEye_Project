package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.AssignComplaintRequest;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.StatusUpdateRequest;
import com.civiceye.ai.complaint.dto.TimelineEventResponse;
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
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
}
