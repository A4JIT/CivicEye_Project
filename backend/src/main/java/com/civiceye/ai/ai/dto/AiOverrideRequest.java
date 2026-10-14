package com.civiceye.ai.ai.dto;

import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.Priority;

public class AiOverrideRequest {

    private ComplaintCategory category;
    private Priority priority;
    private Long departmentId;
    private String reason;
    private String adminNotes;

    public AiOverrideRequest() {
    }

    public AiOverrideRequest(ComplaintCategory category, Priority priority, Long departmentId, String reason, String adminNotes) {
        this.category = category;
        this.priority = priority;
        this.departmentId = departmentId;
        this.reason = reason;
        this.adminNotes = adminNotes;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public static AiOverrideRequestBuilder builder() {
        return new AiOverrideRequestBuilder();
    }

    public static class AiOverrideRequestBuilder {
        private ComplaintCategory category;
        private Priority priority;
        private Long departmentId;
        private String reason;
        private String adminNotes;

        public AiOverrideRequestBuilder category(ComplaintCategory category) { this.category = category; return this; }
        public AiOverrideRequestBuilder priority(Priority priority) { this.priority = priority; return this; }
        public AiOverrideRequestBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public AiOverrideRequestBuilder reason(String reason) { this.reason = reason; return this; }
        public AiOverrideRequestBuilder adminNotes(String adminNotes) { this.adminNotes = adminNotes; return this; }

        public AiOverrideRequest build() {
            return new AiOverrideRequest(this.category, this.priority, this.departmentId, this.reason, this.adminNotes);
        }
    }
}
