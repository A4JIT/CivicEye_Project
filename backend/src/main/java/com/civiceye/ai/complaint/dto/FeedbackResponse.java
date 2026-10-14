package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintFeedback;

import java.time.LocalDateTime;

public class FeedbackResponse {

    private Long id;
    private Long complaintId;
    private String complaintNumber;
    private Long citizenId;
    private String citizenName;
    private Long departmentId;
    private String departmentName;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;

    public FeedbackResponse() {
    }

    public FeedbackResponse(Long id, Long complaintId, String complaintNumber, Long citizenId, String citizenName,
                            Long departmentId, String departmentName, Integer rating, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.complaintNumber = complaintNumber;
        this.citizenId = citizenId;
        this.citizenName = citizenName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public static FeedbackResponse fromEntity(ComplaintFeedback feedback) {
        if (feedback == null) return null;
        FeedbackResponse res = new FeedbackResponse();
        res.setId(feedback.getId());
        if (feedback.getComplaint() != null) {
            res.setComplaintId(feedback.getComplaint().getId());
            res.setComplaintNumber(feedback.getComplaint().getComplaintNumber());
        }
        if (feedback.getCitizen() != null) {
            res.setCitizenId(feedback.getCitizen().getId());
            res.setCitizenName(feedback.getCitizen().getFullName());
        }
        if (feedback.getDepartment() != null) {
            res.setDepartmentId(feedback.getDepartment().getId());
            res.setDepartmentName(feedback.getDepartment().getName());
        }
        res.setRating(feedback.getRating());
        res.setComments(feedback.getComments());
        res.setCreatedAt(feedback.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getComplaintNumber() { return complaintNumber; }
    public void setComplaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; }
    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }
    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static FeedbackResponseBuilder builder() {
        return new FeedbackResponseBuilder();
    }

    public static class FeedbackResponseBuilder {
        private Long id;
        private Long complaintId;
        private String complaintNumber;
        private Long citizenId;
        private String citizenName;
        private Long departmentId;
        private String departmentName;
        private Integer rating;
        private String comments;
        private LocalDateTime createdAt;

        public FeedbackResponseBuilder id(Long id) { this.id = id; return this; }
        public FeedbackResponseBuilder complaintId(Long complaintId) { this.complaintId = complaintId; return this; }
        public FeedbackResponseBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public FeedbackResponseBuilder citizenId(Long citizenId) { this.citizenId = citizenId; return this; }
        public FeedbackResponseBuilder citizenName(String citizenName) { this.citizenName = citizenName; return this; }
        public FeedbackResponseBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public FeedbackResponseBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public FeedbackResponseBuilder rating(Integer rating) { this.rating = rating; return this; }
        public FeedbackResponseBuilder comments(String comments) { this.comments = comments; return this; }
        public FeedbackResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public FeedbackResponse build() {
            return new FeedbackResponse(id, complaintId, complaintNumber, citizenId, citizenName, departmentId, departmentName, rating, comments, createdAt);
        }
    }
}
