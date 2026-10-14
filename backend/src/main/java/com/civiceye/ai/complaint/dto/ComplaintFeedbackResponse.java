package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintFeedback;

import java.time.LocalDateTime;

public class ComplaintFeedbackResponse {

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

    public ComplaintFeedbackResponse() {
    }

    public static ComplaintFeedbackResponse fromEntity(ComplaintFeedback feedback) {
        if (feedback == null) return null;
        ComplaintFeedbackResponse res = new ComplaintFeedbackResponse();
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
}
