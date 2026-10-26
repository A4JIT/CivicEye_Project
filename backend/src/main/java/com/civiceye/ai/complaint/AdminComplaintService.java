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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public AdminComplaintService(ComplaintRepository complaintRepository,
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

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> searchComplaints(List<ComplaintStatus> statuses,
                                                    Long departmentId,
                                                    Priority priority,
                                                    ComplaintCategory category,
                                                    String search,
                                                    Pageable pageable) {
        Specification<Complaint> spec = Specification.where(ComplaintSpecifications.withStatuses(statuses))
                .and(ComplaintSpecifications.withDepartment(departmentId))
                .and(ComplaintSpecifications.withPriority(priority))
                .and(ComplaintSpecifications.withCategory(category))
                .and(ComplaintSpecifications.withSearch(search));

        return complaintRepository.findAll(spec, pageable).map(ComplaintResponse::fromEntity);
    }

    @Transactional
    public ComplaintResponse reviewComplaint(Long complaintId, AdminReviewRequest request, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        ComplaintStatus oldStatus = complaint.getStatus();
        if (oldStatus == ComplaintStatus.REJECTED || oldStatus == ComplaintStatus.CLOSED) {
            throw new BadRequestException("Cannot review complaint that is already " + oldStatus);
        }

        // Advance to UNDER_REVIEW if currently SUBMITTED or NEEDS_INFORMATION
        if (oldStatus == ComplaintStatus.SUBMITTED || oldStatus == ComplaintStatus.NEEDS_INFORMATION) {
            complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
        }

        if (request != null) {
            if (request.getCategory() != null && request.getCategory() != complaint.getCategory()) {
                complaint.setCategory(request.getCategory());
                complaint.setAiCategoryOverridden(true);
            }
            if (request.getPriority() != null && request.getPriority() != complaint.getPriority()) {
                complaint.setPriority(request.getPriority());
                complaint.setAiPriorityOverridden(true);
            }
            if (request.getNotes() != null && !request.getNotes().isBlank()) {
                complaint.setAdminNotes(request.getNotes().trim());
            }
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        StringBuilder remarks = new StringBuilder("Complaint reviewed by administrator");
        if (request != null && request.getNotes() != null && !request.getNotes().isBlank()) {
            remarks.append(". Notes: ").append(request.getNotes().trim());
        }

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(savedComplaint.getStatus())
                .changedBy(actingAdmin)
                .remarks(remarks.toString())
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse rejectComplaint(Long complaintId, RejectComplaintRequest request, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BadRequestException("Rejection reason is mandatory and cannot be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        ComplaintStatus oldStatus = complaint.getStatus();
        if (oldStatus == ComplaintStatus.RESOLVED || oldStatus == ComplaintStatus.CLOSED) {
            throw new BadRequestException("Cannot reject a complaint that is already " + oldStatus);
        }

        String reason = request.getRejectionReason().trim();
        if (request.getRejectionCategory() != null && !request.getRejectionCategory().isBlank()) {
            reason = "[" + request.getRejectionCategory().trim() + "] " + reason;
        }

        complaint.setStatus(ComplaintStatus.REJECTED);
        complaint.setRejectionReason(reason);

        Complaint savedComplaint = complaintRepository.save(complaint);
        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.REJECTED)
                .changedBy(actingAdmin)
                .remarks("Rejected: " + reason)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse requestInformation(Long complaintId, RequestInfoRequest request, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        if (request == null || request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Information request message is mandatory and cannot be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        ComplaintStatus oldStatus = complaint.getStatus();
        if (oldStatus == ComplaintStatus.RESOLVED || oldStatus == ComplaintStatus.CLOSED || oldStatus == ComplaintStatus.REJECTED) {
            throw new BadRequestException("Cannot request information on a " + oldStatus + " complaint");
        }

        String message = request.getRemarks().trim();
        complaint.setStatus(ComplaintStatus.NEEDS_INFORMATION);
        complaint.setNeedsInfoRemarks(message);

        Complaint savedComplaint = complaintRepository.save(complaint);
        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.NEEDS_INFORMATION)
                .changedBy(actingAdmin)
                .remarks("Additional information requested: " + message)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse assignDepartment(Long complaintId, AdminAssignRequest request, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        if (request == null || request.getDepartmentId() == null) {
            throw new BadRequestException("Department ID is required");
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

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setDepartment(department);
        if (assignedOfficer != null) {
            complaint.setAssignedTo(assignedOfficer);
        }

        if (request.getPriority() != null && request.getPriority() != complaint.getPriority()) {
            complaint.setPriority(request.getPriority());
            complaint.setAiPriorityOverridden(true);
        }

        // Advance to ASSIGNED if in early stages
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED
                || complaint.getStatus() == ComplaintStatus.UNDER_REVIEW
                || complaint.getStatus() == ComplaintStatus.NEEDS_INFORMATION) {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        StringBuilder remarks = new StringBuilder("Assigned to ")
                .append(department.getName());
        if (assignedOfficer != null) {
            remarks.append(" (Officer: ").append(assignedOfficer.getFullName()).append(")");
        }
        if (request.getInstructions() != null && !request.getInstructions().isBlank()) {
            remarks.append(". Instructions: ").append(request.getInstructions().trim());
        }

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(savedComplaint.getStatus())
                .changedBy(actingAdmin)
                .remarks(remarks.toString())
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse provideInformation(Long complaintId,
                                                ProvideInfoRequest request,
                                                MultipartFile additionalImage,
                                                UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (request == null || request.getRemarks() == null || request.getRemarks().isBlank()) {
            throw new BadRequestException("Information response message is mandatory and cannot be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (complaint.getStatus() != ComplaintStatus.NEEDS_INFORMATION) {
            throw new BadRequestException("Complaint is currently in status " + complaint.getStatus() + ", not awaiting information");
        }

        if (currentUser.getRole() != Role.ADMIN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to provide information for this complaint");
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.UNDER_REVIEW);

        String storedImageUrl = request.getAdditionalImageUrl();
        if (additionalImage != null && !additionalImage.isEmpty()) {
            String storedName = fileStorageService.storeFile(additionalImage);
            storedImageUrl = "/api/files/" + storedName;
            complaint.setImageUrl(storedImageUrl);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        User respondingUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.UNDER_REVIEW)
                .changedBy(respondingUser)
                .remarks("Citizen provided additional information: " + request.getRemarks().trim())
                .resolutionImageUrl(storedImageUrl)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    private void verifyAdmin(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Access denied: Administrative privileges required");
        }
    }
}
