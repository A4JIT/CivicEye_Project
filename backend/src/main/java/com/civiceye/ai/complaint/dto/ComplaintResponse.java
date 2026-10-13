package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.Complaint;
import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.ComplaintStatus;
import com.civiceye.ai.complaint.Priority;

import java.time.LocalDateTime;

public class ComplaintResponse {

    private Long id;
    private String complaintNumber;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintStatus status;
    private Priority priority;
    private String location;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;
    private Long departmentId;
    private String departmentName;
    private String rejectionReason;
    private String resolutionNotes;
    private String resolutionImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public ComplaintResponse() {
    }

    public static ComplaintResponse fromEntity(Complaint complaint) {
        if (complaint == null) return null;
        ComplaintResponse res = new ComplaintResponse();
        res.setId(complaint.getId());
        res.setComplaintNumber(complaint.getComplaintNumber());
        res.setTitle(complaint.getTitle());
        res.setDescription(complaint.getDescription());
        res.setCategory(complaint.getCategory());
        res.setStatus(complaint.getStatus());
        res.setPriority(complaint.getPriority());
        res.setLocation(complaint.getLocation());
        res.setLatitude(complaint.getLatitude());
        res.setLongitude(complaint.getLongitude());
        res.setImageUrl(complaint.getImageUrl());
        if (complaint.getCitizen() != null) {
            res.setCitizenId(complaint.getCitizen().getId());
            res.setCitizenName(complaint.getCitizen().getFullName());
            res.setCitizenEmail(complaint.getCitizen().getEmail());
        }
        if (complaint.getDepartment() != null) {
            res.setDepartmentId(complaint.getDepartment().getId());
            res.setDepartmentName(complaint.getDepartment().getName());
        }
        res.setRejectionReason(complaint.getRejectionReason());
        res.setResolutionNotes(complaint.getResolutionNotes());
        res.setResolutionImageUrl(complaint.getResolutionImageUrl());
        res.setCreatedAt(complaint.getCreatedAt());
        res.setUpdatedAt(complaint.getUpdatedAt());
        res.setResolvedAt(complaint.getResolvedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getComplaintNumber() { return complaintNumber; }
    public void setComplaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }
    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }
    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
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

    public static ComplaintResponseBuilder builder() {
        return new ComplaintResponseBuilder();
    }

    public static class ComplaintResponseBuilder {
        private Long id;
        private String complaintNumber;
        private String title;
        private String description;
        private ComplaintCategory category;
        private ComplaintStatus status;
        private Priority priority;
        private String location;
        private Double latitude;
        private Double longitude;
        private String imageUrl;
        private Long citizenId;
        private String citizenName;
        private String citizenEmail;
        private Long departmentId;
        private String departmentName;
        private String rejectionReason;
        private String resolutionNotes;
        private String resolutionImageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;

        public ComplaintResponseBuilder id(Long id) { this.id = id; return this; }
        public ComplaintResponseBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public ComplaintResponseBuilder title(String title) { this.title = title; return this; }
        public ComplaintResponseBuilder description(String description) { this.description = description; return this; }
        public ComplaintResponseBuilder category(ComplaintCategory category) { this.category = category; return this; }
        public ComplaintResponseBuilder status(ComplaintStatus status) { this.status = status; return this; }
        public ComplaintResponseBuilder priority(Priority priority) { this.priority = priority; return this; }
        public ComplaintResponseBuilder location(String location) { this.location = location; return this; }
        public ComplaintResponseBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public ComplaintResponseBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public ComplaintResponseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public ComplaintResponseBuilder citizenId(Long citizenId) { this.citizenId = citizenId; return this; }
        public ComplaintResponseBuilder citizenName(String citizenName) { this.citizenName = citizenName; return this; }
        public ComplaintResponseBuilder citizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; return this; }
        public ComplaintResponseBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public ComplaintResponseBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public ComplaintResponseBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public ComplaintResponseBuilder resolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; return this; }
        public ComplaintResponseBuilder resolutionImageUrl(String resolutionImageUrl) { this.resolutionImageUrl = resolutionImageUrl; return this; }
        public ComplaintResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ComplaintResponseBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public ComplaintResponse build() {
            ComplaintResponse res = new ComplaintResponse();
            res.setId(this.id);
            res.setComplaintNumber(this.complaintNumber);
            res.setTitle(this.title);
            res.setDescription(this.description);
            res.setCategory(this.category);
            res.setStatus(this.status);
            res.setPriority(this.priority);
            res.setLocation(this.location);
            res.setLatitude(this.latitude);
            res.setLongitude(this.longitude);
            res.setImageUrl(this.imageUrl);
            res.setCitizenId(this.citizenId);
            res.setCitizenName(this.citizenName);
            res.setCitizenEmail(this.citizenEmail);
            res.setDepartmentId(this.departmentId);
            res.setDepartmentName(this.departmentName);
            res.setRejectionReason(this.rejectionReason);
            res.setResolutionNotes(this.resolutionNotes);
            res.setResolutionImageUrl(this.resolutionImageUrl);
            res.setCreatedAt(this.createdAt);
            res.setUpdatedAt(this.updatedAt);
            res.setResolvedAt(this.resolvedAt);
            return res;
        }
    }
}
