package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.ComplaintStatus;
import com.civiceye.ai.complaint.Priority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplaintTimelineResponse {

    private Long complaintId;
    private String complaintNumber;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintStatus currentStatus;
    private Priority priority;
    private String location;
    private Long citizenId;
    private String citizenName;
    private Long departmentId;
    private String departmentName;
    private Long assignedToId;
    private String assignedToName;
    private String rejectionReason;
    private String resolutionNotes;
    private String resolutionImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    private List<TimelineEventDto> events = new ArrayList<>();
    private List<TimelineEventResponse> statusHistory = new ArrayList<>();
    private List<ComplaintAssignmentResponse> assignments = new ArrayList<>();

    public ComplaintTimelineResponse() {
    }

    public ComplaintTimelineResponse(Long complaintId, String complaintNumber, String title, String description,
                                     ComplaintCategory category, ComplaintStatus currentStatus, Priority priority,
                                     String location, Long citizenId, String citizenName, Long departmentId,
                                     String departmentName, Long assignedToId, String assignedToName,
                                     String rejectionReason, String resolutionNotes, String resolutionImageUrl,
                                     LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime resolvedAt,
                                     LocalDateTime closedAt, List<TimelineEventDto> events,
                                     List<TimelineEventResponse> statusHistory, List<ComplaintAssignmentResponse> assignments) {
        this.complaintId = complaintId;
        this.complaintNumber = complaintNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.currentStatus = currentStatus;
        this.priority = priority;
        this.location = location;
        this.citizenId = citizenId;
        this.citizenName = citizenName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.rejectionReason = rejectionReason;
        this.resolutionNotes = resolutionNotes;
        this.resolutionImageUrl = resolutionImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.events = events != null ? events : new ArrayList<>();
        this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>();
        this.assignments = assignments != null ? assignments : new ArrayList<>();
    }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public String getComplaintNumber() { return complaintNumber; }
    public void setComplaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public ComplaintStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(ComplaintStatus currentStatus) { this.currentStatus = currentStatus; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolutionImageUrl() { return resolutionImageUrl; }
    public void setResolutionImageUrl(String resolutionImageUrl) { this.resolutionImageUrl = resolutionImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public List<TimelineEventDto> getEvents() { return events; }
    public void setEvents(List<TimelineEventDto> events) { this.events = events != null ? events : new ArrayList<>(); }

    public List<TimelineEventResponse> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<TimelineEventResponse> statusHistory) { this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>(); }

    public List<ComplaintAssignmentResponse> getAssignments() { return assignments; }
    public void setAssignments(List<ComplaintAssignmentResponse> assignments) { this.assignments = assignments != null ? assignments : new ArrayList<>(); }

    public static ComplaintTimelineResponseBuilder builder() {
        return new ComplaintTimelineResponseBuilder();
    }

    public static class ComplaintTimelineResponseBuilder {
        private Long complaintId;
        private String complaintNumber;
        private String title;
        private String description;
        private ComplaintCategory category;
        private ComplaintStatus currentStatus;
        private Priority priority;
        private String location;
        private Long citizenId;
        private String citizenName;
        private Long departmentId;
        private String departmentName;
        private Long assignedToId;
        private String assignedToName;
        private String rejectionReason;
        private String resolutionNotes;
        private String resolutionImageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
        private LocalDateTime closedAt;
        private List<TimelineEventDto> events = new ArrayList<>();
        private List<TimelineEventResponse> statusHistory = new ArrayList<>();
        private List<ComplaintAssignmentResponse> assignments = new ArrayList<>();

        public ComplaintTimelineResponseBuilder complaintId(Long complaintId) { this.complaintId = complaintId; return this; }
        public ComplaintTimelineResponseBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public ComplaintTimelineResponseBuilder title(String title) { this.title = title; return this; }
        public ComplaintTimelineResponseBuilder description(String description) { this.description = description; return this; }
        public ComplaintTimelineResponseBuilder category(ComplaintCategory category) { this.category = category; return this; }
        public ComplaintTimelineResponseBuilder currentStatus(ComplaintStatus currentStatus) { this.currentStatus = currentStatus; return this; }
        public ComplaintTimelineResponseBuilder priority(Priority priority) { this.priority = priority; return this; }
        public ComplaintTimelineResponseBuilder location(String location) { this.location = location; return this; }
        public ComplaintTimelineResponseBuilder citizenId(Long citizenId) { this.citizenId = citizenId; return this; }
        public ComplaintTimelineResponseBuilder citizenName(String citizenName) { this.citizenName = citizenName; return this; }
        public ComplaintTimelineResponseBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public ComplaintTimelineResponseBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public ComplaintTimelineResponseBuilder assignedToId(Long assignedToId) { this.assignedToId = assignedToId; return this; }
        public ComplaintTimelineResponseBuilder assignedToName(String assignedToName) { this.assignedToName = assignedToName; return this; }
        public ComplaintTimelineResponseBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public ComplaintTimelineResponseBuilder resolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; return this; }
        public ComplaintTimelineResponseBuilder resolutionImageUrl(String resolutionImageUrl) { this.resolutionImageUrl = resolutionImageUrl; return this; }
        public ComplaintTimelineResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintTimelineResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ComplaintTimelineResponseBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public ComplaintTimelineResponseBuilder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public ComplaintTimelineResponseBuilder events(List<TimelineEventDto> events) { this.events = events != null ? events : new ArrayList<>(); return this; }
        public ComplaintTimelineResponseBuilder statusHistory(List<TimelineEventResponse> statusHistory) { this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>(); return this; }
        public ComplaintTimelineResponseBuilder assignments(List<ComplaintAssignmentResponse> assignments) { this.assignments = assignments != null ? assignments : new ArrayList<>(); return this; }

        public ComplaintTimelineResponse build() {
            return new ComplaintTimelineResponse(complaintId, complaintNumber, title, description, category,
                    currentStatus, priority, location, citizenId, citizenName, departmentId, departmentName,
                    assignedToId, assignedToName, rejectionReason, resolutionNotes, resolutionImageUrl,
                    createdAt, updatedAt, resolvedAt, closedAt, events, statusHistory, assignments);
        }
    }
}
