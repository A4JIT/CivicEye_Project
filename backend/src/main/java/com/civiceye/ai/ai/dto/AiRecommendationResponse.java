package com.civiceye.ai.ai.dto;

import com.civiceye.ai.ai.AiRecommendation;

import java.time.LocalDateTime;

public class AiRecommendationResponse {

    private Long id;
    private Long complaintId;
    private String suggestedCategory;
    private String suggestedPriority;
    private Long suggestedDepartmentId;
    private String suggestedDepartmentCode;
    private String suggestedDepartmentName;
    private Double confidenceScore;
    private String explanation;
    private Boolean isOverridden;
    private Long overriddenByUserId;
    private String overriddenByUserName;
    private LocalDateTime overriddenAt;
    private String overrideReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AiRecommendationResponse() {
    }

    public static AiRecommendationResponse fromEntity(AiRecommendation entity) {
        if (entity == null) return null;

        AiRecommendationResponse res = new AiRecommendationResponse();
        res.setId(entity.getId());
        if (entity.getComplaint() != null) {
            res.setComplaintId(entity.getComplaint().getId());
        }
        res.setSuggestedCategory(entity.getSuggestedCategory());
        res.setSuggestedPriority(entity.getSuggestedPriority());
        if (entity.getSuggestedDepartment() != null) {
            res.setSuggestedDepartmentId(entity.getSuggestedDepartment().getId());
            res.setSuggestedDepartmentCode(entity.getSuggestedDepartment().getCode());
            res.setSuggestedDepartmentName(entity.getSuggestedDepartment().getName());
        }
        res.setConfidenceScore(entity.getConfidenceScore());
        res.setExplanation(entity.getExplanation());
        res.setIsOverridden(entity.getIsOverridden() != null ? entity.getIsOverridden() : false);
        if (entity.getOverriddenBy() != null) {
            res.setOverriddenByUserId(entity.getOverriddenBy().getId());
            res.setOverriddenByUserName(entity.getOverriddenBy().getFullName());
        }
        res.setOverriddenAt(entity.getOverriddenAt());
        res.setOverrideReason(entity.getOverrideReason());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }
    public String getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; }
    public Long getSuggestedDepartmentId() { return suggestedDepartmentId; }
    public void setSuggestedDepartmentId(Long suggestedDepartmentId) { this.suggestedDepartmentId = suggestedDepartmentId; }
    public String getSuggestedDepartmentCode() { return suggestedDepartmentCode; }
    public void setSuggestedDepartmentCode(String suggestedDepartmentCode) { this.suggestedDepartmentCode = suggestedDepartmentCode; }
    public String getSuggestedDepartmentName() { return suggestedDepartmentName; }
    public void setSuggestedDepartmentName(String suggestedDepartmentName) { this.suggestedDepartmentName = suggestedDepartmentName; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Boolean getIsOverridden() { return isOverridden; }
    public void setIsOverridden(Boolean isOverridden) { this.isOverridden = isOverridden; }
    public Long getOverriddenByUserId() { return overriddenByUserId; }
    public void setOverriddenByUserId(Long overriddenByUserId) { this.overriddenByUserId = overriddenByUserId; }
    public String getOverriddenByUserName() { return overriddenByUserName; }
    public void setOverriddenByUserName(String overriddenByUserName) { this.overriddenByUserName = overriddenByUserName; }
    public LocalDateTime getOverriddenAt() { return overriddenAt; }
    public void setOverriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; }
    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static AiRecommendationResponseBuilder builder() {
        return new AiRecommendationResponseBuilder();
    }

    public static class AiRecommendationResponseBuilder {
        private Long id;
        private Long complaintId;
        private String suggestedCategory;
        private String suggestedPriority;
        private Long suggestedDepartmentId;
        private String suggestedDepartmentCode;
        private String suggestedDepartmentName;
        private Double confidenceScore;
        private String explanation;
        private Boolean isOverridden;
        private Long overriddenByUserId;
        private String overriddenByUserName;
        private LocalDateTime overriddenAt;
        private String overrideReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AiRecommendationResponseBuilder id(Long id) { this.id = id; return this; }
        public AiRecommendationResponseBuilder complaintId(Long complaintId) { this.complaintId = complaintId; return this; }
        public AiRecommendationResponseBuilder suggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; return this; }
        public AiRecommendationResponseBuilder suggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; return this; }
        public AiRecommendationResponseBuilder suggestedDepartmentId(Long suggestedDepartmentId) { this.suggestedDepartmentId = suggestedDepartmentId; return this; }
        public AiRecommendationResponseBuilder suggestedDepartmentCode(String suggestedDepartmentCode) { this.suggestedDepartmentCode = suggestedDepartmentCode; return this; }
        public AiRecommendationResponseBuilder suggestedDepartmentName(String suggestedDepartmentName) { this.suggestedDepartmentName = suggestedDepartmentName; return this; }
        public AiRecommendationResponseBuilder confidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public AiRecommendationResponseBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public AiRecommendationResponseBuilder isOverridden(Boolean isOverridden) { this.isOverridden = isOverridden; return this; }
        public AiRecommendationResponseBuilder overriddenByUserId(Long overriddenByUserId) { this.overriddenByUserId = overriddenByUserId; return this; }
        public AiRecommendationResponseBuilder overriddenByUserName(String overriddenByUserName) { this.overriddenByUserName = overriddenByUserName; return this; }
        public AiRecommendationResponseBuilder overriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; return this; }
        public AiRecommendationResponseBuilder overrideReason(String overrideReason) { this.overrideReason = overrideReason; return this; }
        public AiRecommendationResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AiRecommendationResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AiRecommendationResponse build() {
            AiRecommendationResponse res = new AiRecommendationResponse();
            res.setId(this.id);
            res.setComplaintId(this.complaintId);
            res.setSuggestedCategory(this.suggestedCategory);
            res.setSuggestedPriority(this.suggestedPriority);
            res.setSuggestedDepartmentId(this.suggestedDepartmentId);
            res.setSuggestedDepartmentCode(this.suggestedDepartmentCode);
            res.setSuggestedDepartmentName(this.suggestedDepartmentName);
            res.setConfidenceScore(this.confidenceScore);
            res.setExplanation(this.explanation);
            res.setIsOverridden(this.isOverridden);
            res.setOverriddenByUserId(this.overriddenByUserId);
            res.setOverriddenByUserName(this.overriddenByUserName);
            res.setOverriddenAt(this.overriddenAt);
            res.setOverrideReason(this.overrideReason);
            res.setCreatedAt(this.createdAt);
            res.setUpdatedAt(this.updatedAt);
            return res;
        }
    }
}
