package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.Complaint;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", unique = true)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonIgnore
    private Complaint complaint;

    @Column(name = "suggested_category", nullable = false, length = 50)
    private String suggestedCategory;

    @Column(name = "suggested_priority", nullable = false, length = 20)
    @Builder.Default
    private String suggestedPriority = "MEDIUM";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_department_id")
    private Department suggestedDepartment;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "is_overridden", nullable = false)
    @Builder.Default
    private Boolean isOverridden = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by_user_id")
    private User overriddenBy;

    @Column(name = "overridden_at")
    private LocalDateTime overriddenAt;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AiRecommendation() {
        this.suggestedPriority = "MEDIUM";
        this.isOverridden = false;
        this.confidenceScore = 0.50;
    }

    public AiRecommendation(String recommendedCategory, double confidenceScore, String explanation) {
        this.suggestedCategory = recommendedCategory;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
        this.suggestedPriority = "MEDIUM";
        this.isOverridden = false;
    }

    public AiRecommendation(String suggestedCategory, String suggestedPriority, Department suggestedDepartment, double confidenceScore, String explanation) {
        this.suggestedCategory = suggestedCategory;
        this.suggestedPriority = suggestedPriority != null ? suggestedPriority : "MEDIUM";
        this.suggestedDepartment = suggestedDepartment;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
        this.isOverridden = false;
    }

    // Backwards-compatible aliases for recommendedCategory
    public String getRecommendedCategory() {
        return suggestedCategory;
    }

    public void setRecommendedCategory(String recommendedCategory) {
        this.suggestedCategory = recommendedCategory;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    public String getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }
    public String getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; }
    public Department getSuggestedDepartment() { return suggestedDepartment; }
    public void setSuggestedDepartment(Department suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public Boolean getIsOverridden() { return isOverridden; }
    public void setIsOverridden(Boolean isOverridden) { this.isOverridden = isOverridden; }
    public boolean isOverridden() { return isOverridden != null && isOverridden; }
    public User getOverriddenBy() { return overriddenBy; }
    public void setOverriddenBy(User overriddenBy) { this.overriddenBy = overriddenBy; }
    public LocalDateTime getOverriddenAt() { return overriddenAt; }
    public void setOverriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; }
    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.suggestedPriority == null) {
            this.suggestedPriority = "MEDIUM";
        }
        if (this.isOverridden == null) {
            this.isOverridden = false;
        }
        if (this.confidenceScore == null) {
            this.confidenceScore = 0.50;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static AiRecommendationBuilder builder() {
        return new AiRecommendationBuilder();
    }

    public static class AiRecommendationBuilder {
        private Long id;
        private Complaint complaint;
        private String suggestedCategory;
        private String suggestedPriority = "MEDIUM";
        private Department suggestedDepartment;
        private Double confidenceScore = 0.50;
        private String explanation;
        private Boolean isOverridden = false;
        private User overriddenBy;
        private LocalDateTime overriddenAt;
        private String overrideReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AiRecommendationBuilder id(Long id) { this.id = id; return this; }
        public AiRecommendationBuilder complaint(Complaint complaint) { this.complaint = complaint; return this; }
        public AiRecommendationBuilder suggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; return this; }
        public AiRecommendationBuilder recommendedCategory(String recommendedCategory) { this.suggestedCategory = recommendedCategory; return this; }
        public AiRecommendationBuilder suggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; return this; }
        public AiRecommendationBuilder suggestedDepartment(Department suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; return this; }
        public AiRecommendationBuilder confidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public AiRecommendationBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public AiRecommendationBuilder isOverridden(Boolean isOverridden) { this.isOverridden = isOverridden; return this; }
        public AiRecommendationBuilder overriddenBy(User overriddenBy) { this.overriddenBy = overriddenBy; return this; }
        public AiRecommendationBuilder overriddenAt(LocalDateTime overriddenAt) { this.overriddenAt = overriddenAt; return this; }
        public AiRecommendationBuilder overrideReason(String overrideReason) { this.overrideReason = overrideReason; return this; }
        public AiRecommendationBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AiRecommendationBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public AiRecommendation build() {
            AiRecommendation r = new AiRecommendation();
            r.setId(this.id);
            r.setComplaint(this.complaint);
            r.setSuggestedCategory(this.suggestedCategory);
            r.setSuggestedPriority(this.suggestedPriority != null ? this.suggestedPriority : "MEDIUM");
            r.setSuggestedDepartment(this.suggestedDepartment);
            r.setConfidenceScore(this.confidenceScore != null ? this.confidenceScore : 0.50);
            r.setExplanation(this.explanation);
            r.setIsOverridden(this.isOverridden != null ? this.isOverridden : false);
            r.setOverriddenBy(this.overriddenBy);
            r.setOverriddenAt(this.overriddenAt);
            r.setOverrideReason(this.overrideReason);
            r.setCreatedAt(this.createdAt);
            r.setUpdatedAt(this.updatedAt);
            return r;
        }
    }
}
