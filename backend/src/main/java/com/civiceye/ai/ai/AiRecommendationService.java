package com.civiceye.ai.ai;

import com.civiceye.ai.ai.dto.AiOverrideRequest;
import com.civiceye.ai.ai.dto.AiRecommendRequest;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;
import com.civiceye.ai.ai.rules.CategoryKeywords;
import com.civiceye.ai.complaint.*;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AiRecommendationService {

    private final DepartmentRecommendationService departmentRecommendationService;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AiRecommendationService(
            DepartmentRecommendationService departmentRecommendationService,
            AiRecommendationRepository aiRecommendationRepository,
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository statusHistoryRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository) {
        this.departmentRecommendationService = departmentRecommendationService;
        this.aiRecommendationRepository = aiRecommendationRepository;
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public AiRecommendationResponse calculateRecommendation(AiRecommendRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Complaint title is required for AI recommendation analysis");
        }

        AiRecommendation rec = departmentRecommendationService.buildFullRecommendation(
                request.getTitle(),
                request.getDescription(),
                request.getLocation()
        );

        return AiRecommendationResponse.fromEntity(rec);
    }

    @Transactional
    public AiRecommendationResponse getComplaintRecommendation(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        verifyAccess(complaint, currentUser);

        AiRecommendation rec = aiRecommendationRepository.findByComplaintId(complaintId)
                .orElseGet(() -> {
                    AiRecommendation generated = departmentRecommendationService.buildFullRecommendation(
                            complaint.getTitle(),
                            complaint.getDescription(),
                            complaint.getLocation()
                    );
                    generated.setComplaint(complaint);
                    return aiRecommendationRepository.save(generated);
                });

        return AiRecommendationResponse.fromEntity(rec);
    }

    @Transactional
    public ComplaintResponse applyRecommendation(Long complaintId, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        AiRecommendation rec = aiRecommendationRepository.findByComplaintId(complaintId)
                .orElseGet(() -> {
                    AiRecommendation generated = departmentRecommendationService.buildFullRecommendation(
                            complaint.getTitle(),
                            complaint.getDescription(),
                            complaint.getLocation()
                    );
                    generated.setComplaint(complaint);
                    return aiRecommendationRepository.save(generated);
                });

        ComplaintStatus oldStatus = complaint.getStatus();

        // 1. Apply category
        ComplaintCategory category = CategoryKeywords.resolveCategory(rec.getSuggestedCategory());
        complaint.setCategory(category);

        // 2. Apply priority
        try {
            complaint.setPriority(Priority.valueOf(rec.getSuggestedPriority()));
        } catch (Exception ex) {
            complaint.setPriority(Priority.MEDIUM);
        }

        // 3. Apply department & update status
        if (rec.getSuggestedDepartment() != null) {
            complaint.setDepartment(rec.getSuggestedDepartment());
            if (oldStatus == ComplaintStatus.SUBMITTED || oldStatus == ComplaintStatus.UNDER_REVIEW) {
                complaint.setStatus(ComplaintStatus.ASSIGNED);
            }
        }

        complaint.setAiCategoryOverridden(false);
        complaint.setAiPriorityOverridden(false);

        Complaint savedComplaint = complaintRepository.save(complaint);
        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        if (statusHistoryRepository != null) {
            String deptName = complaint.getDepartment() != null ? complaint.getDepartment().getName() : "None";
            String remarks = String.format("Admin accepted AI recommendation in 1-click triage: Category set to %s, Priority set to %s, Department assigned to %s",
                    complaint.getCategory().name(), complaint.getPriority().name(), deptName);

            ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                    .complaint(savedComplaint)
                    .fromStatus(oldStatus)
                    .toStatus(savedComplaint.getStatus())
                    .changedBy(actingAdmin)
                    .remarks(remarks)
                    .createdAt(LocalDateTime.now())
                    .build();
            statusHistoryRepository.save(history);
        }

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse overrideRecommendation(Long complaintId, AiOverrideRequest request, UserPrincipal adminUser) {
        verifyAdmin(adminUser);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        User actingAdmin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUser.getId()));

        AiRecommendation rec = aiRecommendationRepository.findByComplaintId(complaintId)
                .orElseGet(() -> {
                    AiRecommendation generated = departmentRecommendationService.buildFullRecommendation(
                            complaint.getTitle(),
                            complaint.getDescription(),
                            complaint.getLocation()
                    );
                    generated.setComplaint(complaint);
                    return aiRecommendationRepository.save(generated);
                });

        String overrideReason = (request != null && request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason().trim()
                : "Administrative override during triage";

        rec.setIsOverridden(true);
        rec.setOverriddenBy(actingAdmin);
        rec.setOverriddenAt(LocalDateTime.now());
        rec.setOverrideReason(overrideReason);
        aiRecommendationRepository.save(rec);

        ComplaintStatus oldStatus = complaint.getStatus();

        if (request != null) {
            if (request.getCategory() != null) {
                complaint.setCategory(request.getCategory());
                complaint.setAiCategoryOverridden(true);
            }
            if (request.getPriority() != null) {
                complaint.setPriority(request.getPriority());
                complaint.setAiPriorityOverridden(true);
            }
            if (request.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
                complaint.setDepartment(dept);
                if (oldStatus == ComplaintStatus.SUBMITTED || oldStatus == ComplaintStatus.UNDER_REVIEW) {
                    complaint.setStatus(ComplaintStatus.ASSIGNED);
                }
            }
            if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
                complaint.setAdminNotes(request.getAdminNotes().trim());
            }
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        if (statusHistoryRepository != null) {
            String remarks = "Administrative override of AI recommendation: " + overrideReason;
            ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                    .complaint(savedComplaint)
                    .fromStatus(oldStatus)
                    .toStatus(savedComplaint.getStatus())
                    .changedBy(actingAdmin)
                    .remarks(remarks)
                    .createdAt(LocalDateTime.now())
                    .build();
            statusHistoryRepository.save(history);
        }

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

    private void verifyAccess(Complaint complaint, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.DEPARTMENT_USER) {
            User officer = userRepository.findById(currentUser.getId()).orElse(null);
            if (officer != null && officer.getDepartment() != null && complaint.getDepartment() != null) {
                if (complaint.getDepartment().getId().equals(officer.getDepartment().getId())) {
                    return;
                }
            }
            throw new UnauthorizedException("Access denied: Department isolation restriction");
        }
        if (currentUser.getRole() == Role.CITIZEN) {
            if (complaint.getCitizen() != null && complaint.getCitizen().getId().equals(currentUser.getId())) {
                return;
            }
            throw new UnauthorizedException("Access denied: You can only view recommendations for your own complaints");
        }
    }
}
