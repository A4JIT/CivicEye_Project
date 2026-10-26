package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintAssignmentResponse;
import com.civiceye.ai.complaint.dto.ComplaintTimelineResponse;
import com.civiceye.ai.complaint.dto.TimelineEventDto;
import com.civiceye.ai.complaint.dto.TimelineEventResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintHistoryService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final ComplaintAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public ComplaintHistoryService(ComplaintRepository complaintRepository,
                                  ComplaintStatusHistoryRepository statusHistoryRepository,
                                  ComplaintAssignmentRepository assignmentRepository,
                                  DepartmentRepository departmentRepository,
                                  UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentRepository = assignmentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ComplaintStatusHistory recordStatusTransition(Complaint complaint,
                                                         ComplaintStatus fromStatus,
                                                         ComplaintStatus toStatus,
                                                         User changedBy,
                                                         String remarks,
                                                         String resolutionImageUrl) {
        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .resolutionImageUrl(resolutionImageUrl)
                .createdAt(LocalDateTime.now())
                .build();

        ComplaintStatusHistory saved = statusHistoryRepository.save(history);
        complaint.addStatusHistory(saved);
        return saved;
    }

    @Transactional
    public ComplaintAssignment recordAssignment(Complaint complaint,
                                                Department department,
                                                User assignedTo,
                                                User assignedBy,
                                                String instructions) {
        // Mark any existing active assignments as inactive
        assignmentRepository.findByComplaintIdAndIsActiveTrue(complaint.getId()).ifPresent(active -> {
            active.setIsActive(false);
            active.setCompletedAt(LocalDateTime.now());
            assignmentRepository.save(active);
        });

        ComplaintAssignment assignment = ComplaintAssignment.builder()
                .complaint(complaint)
                .department(department)
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .instructions(instructions)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        ComplaintAssignment saved = assignmentRepository.save(assignment);
        complaint.addAssignment(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public ComplaintTimelineResponse getComplaintHistory(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        validateAccess(complaint, currentUser);

        List<ComplaintStatusHistory> statusHistories = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
        List<ComplaintAssignment> assignments = assignmentRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);

        List<TimelineEventDto> timelineEvents = assembleUnifiedTimeline(complaint, statusHistories, assignments);

        List<TimelineEventResponse> statusResponses = statusHistories.stream()
                .map(TimelineEventResponse::fromEntity)
                .collect(Collectors.toList());

        List<ComplaintAssignmentResponse> assignmentResponses = assignments.stream()
                .map(ComplaintAssignmentResponse::fromEntity)
                .collect(Collectors.toList());

        return ComplaintTimelineResponse.builder()
                .complaintId(complaint.getId())
                .complaintNumber(complaint.getComplaintNumber())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory())
                .currentStatus(complaint.getStatus())
                .priority(complaint.getPriority())
                .location(complaint.getLocation())
                .citizenId(complaint.getCitizen() != null ? complaint.getCitizen().getId() : null)
                .citizenName(complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : null)
                .departmentId(complaint.getDepartment() != null ? complaint.getDepartment().getId() : null)
                .departmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : null)
                .assignedToId(complaint.getAssignedTo() != null ? complaint.getAssignedTo().getId() : null)
                .assignedToName(complaint.getAssignedTo() != null ? complaint.getAssignedTo().getFullName() : null)
                .rejectionReason(complaint.getRejectionReason())
                .resolutionNotes(complaint.getResolutionNotes())
                .resolutionImageUrl(complaint.getResolutionImageUrl())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .closedAt(complaint.getClosedAt())
                .events(timelineEvents)
                .statusHistory(statusResponses)
                .assignments(assignmentResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ComplaintAssignmentResponse> getAssignmentHistory(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        validateAccess(complaint, currentUser);

        return assignmentRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId).stream()
                .map(ComplaintAssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateAccess(Complaint complaint, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admins have full access
        }

        if (currentUser.getRole() == Role.CITIZEN) {
            if (complaint.getCitizen() == null || !complaint.getCitizen().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied: You can only view history for your own complaints");
            }
            return;
        }

        if (currentUser.getRole() == Role.DEPARTMENT_USER) {
            User officer = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

            if (officer.getDepartment() == null || complaint.getDepartment() == null
                    || !complaint.getDepartment().getId().equals(officer.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You can only view history for complaints assigned to your department");
            }
            return;
        }

        throw new AccessDeniedException("Access denied: Unauthorized role");
    }

    private List<TimelineEventDto> assembleUnifiedTimeline(Complaint complaint,
                                                           List<ComplaintStatusHistory> statusHistories,
                                                           List<ComplaintAssignment> assignments) {
        List<TimelineEventDto> events = new ArrayList<>();

        // 1. Initial Submission Event
        events.add(TimelineEventDto.builder()
                .id(0L)
                .timestamp(complaint.getCreatedAt())
                .eventType("SUBMITTED")
                .actorRole("CITIZEN")
                .actorId(complaint.getCitizen() != null ? complaint.getCitizen().getId() : null)
                .actorName(complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : "Reporting Citizen")
                .toStatus(ComplaintStatus.SUBMITTED)
                .title("Complaint Filed")
                .remarks("Complaint submitted with description and location: " + complaint.getLocation())
                .evidenceUrl(complaint.getImageUrl())
                .build());

        // 2. Status Transition Events
        for (ComplaintStatusHistory sh : statusHistories) {
            String eventType = "STATUS_CHANGE";
            String title = "Status changed to " + sh.getToStatus();

            if (sh.getToStatus() == ComplaintStatus.UNDER_REVIEW) {
                eventType = "UNDER_REVIEW";
                title = "Administrative Review Initiated";
            } else if (sh.getToStatus() == ComplaintStatus.ASSIGNED) {
                eventType = "ASSIGNED";
                title = "Assigned to Department";
            } else if (sh.getToStatus() == ComplaintStatus.IN_PROGRESS) {
                eventType = "IN_PROGRESS";
                title = "Department Work Started";
            } else if (sh.getToStatus() == ComplaintStatus.RESOLVED) {
                eventType = "RESOLVED";
                title = "Complaint Resolved with Proof";
            } else if (sh.getToStatus() == ComplaintStatus.CLOSED) {
                eventType = "CLOSED";
                title = "Complaint Officially Closed";
            } else if (sh.getToStatus() == ComplaintStatus.REJECTED) {
                eventType = "REJECTED";
                title = "Complaint Rejected";
            } else if (sh.getToStatus() == ComplaintStatus.NEEDS_INFORMATION) {
                eventType = "NEEDS_INFORMATION";
                title = "Additional Information Requested";
            }

            events.add(TimelineEventDto.builder()
                    .id(sh.getId())
                    .timestamp(sh.getCreatedAt())
                    .eventType(eventType)
                    .actorRole(sh.getChangedBy() != null && sh.getChangedBy().getRole() != null ? sh.getChangedBy().getRole().name() : null)
                    .actorId(sh.getChangedBy() != null ? sh.getChangedBy().getId() : null)
                    .actorName(sh.getChangedBy() != null ? sh.getChangedBy().getFullName() : null)
                    .fromStatus(sh.getFromStatus())
                    .toStatus(sh.getToStatus())
                    .title(title)
                    .remarks(sh.getRemarks())
                    .evidenceUrl(sh.getResolutionImageUrl())
                    .build());
        }

        // 3. Assignment Events
        for (ComplaintAssignment ca : assignments) {
            events.add(TimelineEventDto.builder()
                    .id(ca.getId())
                    .timestamp(ca.getCreatedAt())
                    .eventType(ca.getIsActive() ? "ASSIGNMENT" : "REASSIGNMENT")
                    .actorRole(ca.getAssignedBy() != null && ca.getAssignedBy().getRole() != null ? ca.getAssignedBy().getRole().name() : "ADMIN")
                    .actorId(ca.getAssignedBy() != null ? ca.getAssignedBy().getId() : null)
                    .actorName(ca.getAssignedBy() != null ? ca.getAssignedBy().getFullName() : null)
                    .title(ca.getIsActive() ? "Department Assignment" : "Prior Department Handover")
                    .departmentId(ca.getDepartment() != null ? ca.getDepartment().getId() : null)
                    .departmentName(ca.getDepartment() != null ? ca.getDepartment().getName() : null)
                    .assignedToId(ca.getAssignedTo() != null ? ca.getAssignedTo().getId() : null)
                    .assignedToName(ca.getAssignedTo() != null ? ca.getAssignedTo().getFullName() : null)
                    .remarks(ca.getInstructions())
                    .build());
        }

        // 4. Citizen Feedback Event (if present)
        if (complaint.getFeedback() != null) {
            events.add(TimelineEventDto.builder()
                    .id(complaint.getFeedback().getId())
                    .timestamp(complaint.getFeedback().getCreatedAt())
                    .eventType("FEEDBACK")
                    .actorRole("CITIZEN")
                    .actorId(complaint.getCitizen() != null ? complaint.getCitizen().getId() : null)
                    .actorName(complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : null)
                    .title("Citizen Feedback Rating: " + complaint.getFeedback().getRating() + " / 5 Stars")
                    .remarks(complaint.getFeedback().getComments())
                    .build());
        }

        // Sort chronologically ascending by timestamp
        events.sort(Comparator.comparing(TimelineEventDto::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));

        return events;
    }
}
