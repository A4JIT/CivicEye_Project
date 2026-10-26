package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.ai.dto.AiRecommendationResponse;
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
    private Long assignedToId;
    private String assignedToName;
    private String rejectionReason;
    private String resolutionNotes;
    private String resolutionImageUrl;
    private String adminNotes;
    private String needsInfoRemarks;
    private Boolean aiCategoryOverridden;
    private Boolean aiPriorityOverridden;
    private LocalDateTime startedWorkingAt;
    private String progressNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private Long closedById;
    private String closedByName;
    private String closureRemarks;
    private Boolean citizenConfirmedResolution;
    private Integer feedbackRating;
    private String feedbackComments;
    private AiRecommendationResponse aiRecommendation;
    private Long duplicateOfId;
    private String duplicateOfNumber;
    private Double duplicateScore;
    private Integer upvotesCount;

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
        if (complaint.getAssignedTo() != null) {
            res.setAssignedToId(complaint.getAssignedTo().getId());
            res.setAssignedToName(complaint.getAssignedTo().getFullName());
        }
        res.setRejectionReason(complaint.getRejectionReason());
        res.setResolutionNotes(complaint.getResolutionNotes());
        res.setResolutionImageUrl(complaint.getResolutionImageUrl());
        res.setAdminNotes(complaint.getAdminNotes());
        res.setNeedsInfoRemarks(complaint.getNeedsInfoRemarks());
        res.setAiCategoryOverridden(complaint.getAiCategoryOverridden() != null ? complaint.getAiCategoryOverridden() : false);
        res.setAiPriorityOverridden(complaint.getAiPriorityOverridden() != null ? complaint.getAiPriorityOverridden() : false);
        res.setStartedWorkingAt(complaint.getStartedWorkingAt());
        res.setProgressNotes(complaint.getProgressNotes());
        res.setCreatedAt(complaint.getCreatedAt());
        res.setUpdatedAt(complaint.getUpdatedAt());
        res.setResolvedAt(complaint.getResolvedAt());
        res.setClosedAt(complaint.getClosedAt());
        if (complaint.getClosedBy() != null) {
            res.setClosedById(complaint.getClosedBy().getId());
            res.setClosedByName(complaint.getClosedBy().getFullName());
        }
        res.setClosureRemarks(complaint.getClosureRemarks());
        res.setCitizenConfirmedResolution(complaint.getCitizenConfirmedResolution() != null ? complaint.getCitizenConfirmedResolution() : false);
        if (complaint.getFeedback() != null) {
            res.setFeedbackRating(complaint.getFeedback().getRating());
            res.setFeedbackComments(complaint.getFeedback().getComments());
        }
        if (complaint.getAiRecommendation() != null) {
            res.setAiRecommendation(AiRecommendationResponse.fromEntity(complaint.getAiRecommendation()));
        }
        if (complaint.getDuplicateOf() != null) {
            res.setDuplicateOfId(complaint.getDuplicateOf().getId());
            res.setDuplicateOfNumber(complaint.getDuplicateOf().getComplaintNumber());
        }
        res.setDuplicateScore(complaint.getDuplicateScore());
        res.setUpvotesCount(complaint.getUpvotesCount() != null ? complaint.getUpvotesCount() : 1);
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
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public String getNeedsInfoRemarks() { return needsInfoRemarks; }
    public void setNeedsInfoRemarks(String needsInfoRemarks) { this.needsInfoRemarks = needsInfoRemarks; }
    public Boolean getAiCategoryOverridden() { return aiCategoryOverridden; }
    public void setAiCategoryOverridden(Boolean aiCategoryOverridden) { this.aiCategoryOverridden = aiCategoryOverridden; }
    public Boolean getAiPriorityOverridden() { return aiPriorityOverridden; }
    public void setAiPriorityOverridden(Boolean aiPriorityOverridden) { this.aiPriorityOverridden = aiPriorityOverridden; }
    public LocalDateTime getStartedWorkingAt() { return startedWorkingAt; }
    public void setStartedWorkingAt(LocalDateTime startedWorkingAt) { this.startedWorkingAt = startedWorkingAt; }
    public String getProgressNotes() { return progressNotes; }
    public void setProgressNotes(String progressNotes) { this.progressNotes = progressNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public Long getClosedById() { return closedById; }
    public void setClosedById(Long closedById) { this.closedById = closedById; }
    public String getClosedByName() { return closedByName; }
    public void setClosedByName(String closedByName) { this.closedByName = closedByName; }
    public String getClosureRemarks() { return closureRemarks; }
    public void setClosureRemarks(String closureRemarks) { this.closureRemarks = closureRemarks; }
    public Boolean getCitizenConfirmedResolution() { return citizenConfirmedResolution; }
    public void setCitizenConfirmedResolution(Boolean citizenConfirmedResolution) { this.citizenConfirmedResolution = citizenConfirmedResolution; }
    public Integer getFeedbackRating() { return feedbackRating; }
    public void setFeedbackRating(Integer feedbackRating) { this.feedbackRating = feedbackRating; }
    public String getFeedbackComments() { return feedbackComments; }
    public void setFeedbackComments(String feedbackComments) { this.feedbackComments = feedbackComments; }
    public AiRecommendationResponse getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(AiRecommendationResponse aiRecommendation) { this.aiRecommendation = aiRecommendation; }
    public Long getDuplicateOfId() { return duplicateOfId; }
    public void setDuplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; }
    public String getDuplicateOfNumber() { return duplicateOfNumber; }
    public void setDuplicateOfNumber(String duplicateOfNumber) { this.duplicateOfNumber = duplicateOfNumber; }
    public Double getDuplicateScore() { return duplicateScore; }
    public void setDuplicateScore(Double duplicateScore) { this.duplicateScore = duplicateScore; }
    public Integer getUpvotesCount() { return upvotesCount; }
    public void setUpvotesCount(Integer upvotesCount) { this.upvotesCount = upvotesCount; }

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
        private Long assignedToId;
        private String assignedToName;
        private String rejectionReason;
        private String resolutionNotes;
        private String resolutionImageUrl;
        private String adminNotes;
        private String needsInfoRemarks;
        private Boolean aiCategoryOverridden = false;
        private Boolean aiPriorityOverridden = false;
        private LocalDateTime startedWorkingAt;
        private String progressNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
        private LocalDateTime closedAt;
        private Long closedById;
        private String closedByName;
        private String closureRemarks;
        private Boolean citizenConfirmedResolution = false;
        private Integer feedbackRating;
        private String feedbackComments;
        private AiRecommendationResponse aiRecommendation;
        private Long duplicateOfId;
        private String duplicateOfNumber;
        private Double duplicateScore;
        private Integer upvotesCount = 1;

        public ComplaintResponseBuilder duplicateOfId(Long duplicateOfId) { this.duplicateOfId = duplicateOfId; return this; }
        public ComplaintResponseBuilder duplicateOfNumber(String duplicateOfNumber) { this.duplicateOfNumber = duplicateOfNumber; return this; }
        public ComplaintResponseBuilder duplicateScore(Double duplicateScore) { this.duplicateScore = duplicateScore; return this; }
        public ComplaintResponseBuilder upvotesCount(Integer upvotesCount) { this.upvotesCount = upvotesCount; return this; }

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
        public ComplaintResponseBuilder assignedToId(Long assignedToId) { this.assignedToId = assignedToId; return this; }
        public ComplaintResponseBuilder assignedToName(String assignedToName) { this.assignedToName = assignedToName; return this; }
        public ComplaintResponseBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public ComplaintResponseBuilder resolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; return this; }
        public ComplaintResponseBuilder resolutionImageUrl(String resolutionImageUrl) { this.resolutionImageUrl = resolutionImageUrl; return this; }
        public ComplaintResponseBuilder adminNotes(String adminNotes) { this.adminNotes = adminNotes; return this; }
        public ComplaintResponseBuilder needsInfoRemarks(String needsInfoRemarks) { this.needsInfoRemarks = needsInfoRemarks; return this; }
        public ComplaintResponseBuilder aiCategoryOverridden(Boolean aiCategoryOverridden) { this.aiCategoryOverridden = aiCategoryOverridden; return this; }
        public ComplaintResponseBuilder aiPriorityOverridden(Boolean aiPriorityOverridden) { this.aiPriorityOverridden = aiPriorityOverridden; return this; }
        public ComplaintResponseBuilder startedWorkingAt(LocalDateTime startedWorkingAt) { this.startedWorkingAt = startedWorkingAt; return this; }
        public ComplaintResponseBuilder progressNotes(String progressNotes) { this.progressNotes = progressNotes; return this; }
        public ComplaintResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ComplaintResponseBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public ComplaintResponseBuilder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public ComplaintResponseBuilder closedById(Long closedById) { this.closedById = closedById; return this; }
        public ComplaintResponseBuilder closedByName(String closedByName) { this.closedByName = closedByName; return this; }
        public ComplaintResponseBuilder closureRemarks(String closureRemarks) { this.closureRemarks = closureRemarks; return this; }
        public ComplaintResponseBuilder citizenConfirmedResolution(Boolean citizenConfirmedResolution) { this.citizenConfirmedResolution = citizenConfirmedResolution; return this; }
        public ComplaintResponseBuilder feedbackRating(Integer feedbackRating) { this.feedbackRating = feedbackRating; return this; }
        public ComplaintResponseBuilder feedbackComments(String feedbackComments) { this.feedbackComments = feedbackComments; return this; }
        public ComplaintResponseBuilder aiRecommendation(AiRecommendationResponse aiRecommendation) { this.aiRecommendation = aiRecommendation; return this; }

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
            res.setAssignedToId(this.assignedToId);
            res.setAssignedToName(this.assignedToName);
            res.setRejectionReason(this.rejectionReason);
            res.setResolutionNotes(this.resolutionNotes);
            res.setResolutionImageUrl(this.resolutionImageUrl);
            res.setAdminNotes(this.adminNotes);
            res.setNeedsInfoRemarks(this.needsInfoRemarks);
            res.setAiCategoryOverridden(this.aiCategoryOverridden != null ? this.aiCategoryOverridden : false);
            res.setAiPriorityOverridden(this.aiPriorityOverridden != null ? this.aiPriorityOverridden : false);
            res.setStartedWorkingAt(this.startedWorkingAt);
            res.setProgressNotes(this.progressNotes);
            res.setCreatedAt(this.createdAt);
            res.setUpdatedAt(this.updatedAt);
            res.setResolvedAt(this.resolvedAt);
            res.setClosedAt(this.closedAt);
            res.setClosedById(this.closedById);
            res.setClosedByName(this.closedByName);
            res.setClosureRemarks(this.closureRemarks);
            res.setCitizenConfirmedResolution(this.citizenConfirmedResolution != null ? this.citizenConfirmedResolution : false);
            res.setFeedbackRating(this.feedbackRating);
            res.setFeedbackComments(this.feedbackComments);
            res.setAiRecommendation(this.aiRecommendation);
            res.setDuplicateOfId(this.duplicateOfId);
            res.setDuplicateOfNumber(this.duplicateOfNumber);
            res.setDuplicateScore(this.duplicateScore);
            res.setUpvotesCount(this.upvotesCount != null ? this.upvotesCount : 1);
            return res;
        }
    }
}
