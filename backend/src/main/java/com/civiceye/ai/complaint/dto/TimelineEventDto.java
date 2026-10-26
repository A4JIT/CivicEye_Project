package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintStatus;

import java.time.LocalDateTime;

public class TimelineEventDto {

    private Long id;
    private LocalDateTime timestamp;
    private String eventType;
    private String actorRole;
    private Long actorId;
    private String actorName;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String title;
    private String remarks;
    private String evidenceUrl;
    private Long departmentId;
    private String departmentName;
    private Long assignedToId;
    private String assignedToName;

    public TimelineEventDto() {
    }

    public TimelineEventDto(Long id, LocalDateTime timestamp, String eventType, String actorRole,
                            Long actorId, String actorName, ComplaintStatus fromStatus, ComplaintStatus toStatus,
                            String title, String remarks, String evidenceUrl, Long departmentId,
                            String departmentName, Long assignedToId, String assignedToName) {
        this.id = id;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.actorRole = actorRole;
        this.actorId = actorId;
        this.actorName = actorName;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.title = title;
        this.remarks = remarks;
        this.evidenceUrl = evidenceUrl;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public ComplaintStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; }

    public ComplaintStatus getToStatus() { return toStatus; }
    public void setToStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public static TimelineEventDtoBuilder builder() {
        return new TimelineEventDtoBuilder();
    }

    public static class TimelineEventDtoBuilder {
        private Long id;
        private LocalDateTime timestamp;
        private String eventType;
        private String actorRole;
        private Long actorId;
        private String actorName;
        private ComplaintStatus fromStatus;
        private ComplaintStatus toStatus;
        private String title;
        private String remarks;
        private String evidenceUrl;
        private Long departmentId;
        private String departmentName;
        private Long assignedToId;
        private String assignedToName;

        public TimelineEventDtoBuilder id(Long id) { this.id = id; return this; }
        public TimelineEventDtoBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public TimelineEventDtoBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public TimelineEventDtoBuilder actorRole(String actorRole) { this.actorRole = actorRole; return this; }
        public TimelineEventDtoBuilder actorId(Long actorId) { this.actorId = actorId; return this; }
        public TimelineEventDtoBuilder actorName(String actorName) { this.actorName = actorName; return this; }
        public TimelineEventDtoBuilder fromStatus(ComplaintStatus fromStatus) { this.fromStatus = fromStatus; return this; }
        public TimelineEventDtoBuilder toStatus(ComplaintStatus toStatus) { this.toStatus = toStatus; return this; }
        public TimelineEventDtoBuilder title(String title) { this.title = title; return this; }
        public TimelineEventDtoBuilder remarks(String remarks) { this.remarks = remarks; return this; }
        public TimelineEventDtoBuilder evidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; return this; }
        public TimelineEventDtoBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public TimelineEventDtoBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public TimelineEventDtoBuilder assignedToId(Long assignedToId) { this.assignedToId = assignedToId; return this; }
        public TimelineEventDtoBuilder assignedToName(String assignedToName) { this.assignedToName = assignedToName; return this; }

        public TimelineEventDto build() {
            return new TimelineEventDto(id, timestamp, eventType, actorRole, actorId, actorName, fromStatus, toStatus,
                    title, remarks, evidenceUrl, departmentId, departmentName, assignedToId, assignedToName);
        }
    }
}
