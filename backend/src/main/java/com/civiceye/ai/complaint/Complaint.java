package com.civiceye.ai.complaint;

import com.civiceye.ai.ai.AiRecommendation;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_number", nullable = false, unique = true, length = 50)
    private String complaintNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, length = 255)
    private String location;

    private Double latitude;

    private Double longitude;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolution_image_url", length = 500)
    private String resolutionImageUrl;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "needs_info_remarks", columnDefinition = "TEXT")
    private String needsInfoRemarks;

    @Column(name = "ai_category_overridden")
    @Builder.Default
    private Boolean aiCategoryOverridden = false;

    @Column(name = "ai_priority_overridden")
    @Builder.Default
    private Boolean aiPriorityOverridden = false;

    @Column(name = "started_working_at")
    private LocalDateTime startedWorkingAt;

    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_user_id")
    private User closedBy;

    @Column(name = "closure_remarks", columnDefinition = "TEXT")
    private String closureRemarks;

    @Column(name = "citizen_confirmed_resolution")
    @Builder.Default
    private Boolean citizenConfirmedResolution = false;

    @OneToOne(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ComplaintFeedback feedback;

    @OneToOne(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AiRecommendation aiRecommendation;

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
    public User getCitizen() { return citizen; }
    public void setCitizen(User citizen) { this.citizen = citizen; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
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
    public User getClosedBy() { return closedBy; }
    public void setClosedBy(User closedBy) { this.closedBy = closedBy; }
    public String getClosureRemarks() { return closureRemarks; }
    public void setClosureRemarks(String closureRemarks) { this.closureRemarks = closureRemarks; }
    public Boolean getCitizenConfirmedResolution() { return citizenConfirmedResolution; }
    public void setCitizenConfirmedResolution(Boolean citizenConfirmedResolution) { this.citizenConfirmedResolution = citizenConfirmedResolution; }
    public ComplaintFeedback getFeedback() { return feedback; }
    public void setFeedback(ComplaintFeedback feedback) {
        this.feedback = feedback;
        if (feedback != null && feedback.getComplaint() != this) {
            feedback.setComplaint(this);
        }
    }
    public AiRecommendation getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(AiRecommendation aiRecommendation) {
        this.aiRecommendation = aiRecommendation;
        if (aiRecommendation != null && aiRecommendation.getComplaint() != this) {
            aiRecommendation.setComplaint(this);
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        if (this.citizenConfirmedResolution == null) {
            this.citizenConfirmedResolution = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ComplaintStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
        if (this.status == ComplaintStatus.CLOSED && this.closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public static ComplaintBuilder builder() {
        return new ComplaintBuilder();
    }

    public static class ComplaintBuilder {
        private Long id;
        private String complaintNumber;
        private String title;
        private String description;
        private ComplaintCategory category;
        private ComplaintStatus status = ComplaintStatus.SUBMITTED;
        private Priority priority = Priority.MEDIUM;
        private String location;
        private Double latitude;
        private Double longitude;
        private String imageUrl;
        private User citizen;
        private Department department;
        private User assignedTo;
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
        private User closedBy;
        private String closureRemarks;
        private Boolean citizenConfirmedResolution = false;
        private ComplaintFeedback feedback;
        private AiRecommendation aiRecommendation;

        public ComplaintBuilder id(Long id) { this.id = id; return this; }
        public ComplaintBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public ComplaintBuilder title(String title) { this.title = title; return this; }
        public ComplaintBuilder description(String description) { this.description = description; return this; }
        public ComplaintBuilder category(ComplaintCategory category) { this.category = category; return this; }
        public ComplaintBuilder status(ComplaintStatus status) { this.status = status; return this; }
        public ComplaintBuilder priority(Priority priority) { this.priority = priority; return this; }
        public ComplaintBuilder location(String location) { this.location = location; return this; }
        public ComplaintBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public ComplaintBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public ComplaintBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public ComplaintBuilder citizen(User citizen) { this.citizen = citizen; return this; }
        public ComplaintBuilder department(Department department) { this.department = department; return this; }
        public ComplaintBuilder assignedTo(User assignedTo) { this.assignedTo = assignedTo; return this; }
        public ComplaintBuilder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public ComplaintBuilder resolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; return this; }
        public ComplaintBuilder resolutionImageUrl(String resolutionImageUrl) { this.resolutionImageUrl = resolutionImageUrl; return this; }
        public ComplaintBuilder adminNotes(String adminNotes) { this.adminNotes = adminNotes; return this; }
        public ComplaintBuilder needsInfoRemarks(String needsInfoRemarks) { this.needsInfoRemarks = needsInfoRemarks; return this; }
        public ComplaintBuilder aiCategoryOverridden(Boolean aiCategoryOverridden) { this.aiCategoryOverridden = aiCategoryOverridden; return this; }
        public ComplaintBuilder aiPriorityOverridden(Boolean aiPriorityOverridden) { this.aiPriorityOverridden = aiPriorityOverridden; return this; }
        public ComplaintBuilder startedWorkingAt(LocalDateTime startedWorkingAt) { this.startedWorkingAt = startedWorkingAt; return this; }
        public ComplaintBuilder progressNotes(String progressNotes) { this.progressNotes = progressNotes; return this; }
        public ComplaintBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ComplaintBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public ComplaintBuilder closedAt(LocalDateTime closedAt) { this.closedAt = closedAt; return this; }
        public ComplaintBuilder closedBy(User closedBy) { this.closedBy = closedBy; return this; }
        public ComplaintBuilder closureRemarks(String closureRemarks) { this.closureRemarks = closureRemarks; return this; }
        public ComplaintBuilder citizenConfirmedResolution(Boolean citizenConfirmedResolution) { this.citizenConfirmedResolution = citizenConfirmedResolution; return this; }
        public ComplaintBuilder feedback(ComplaintFeedback feedback) { this.feedback = feedback; return this; }
        public ComplaintBuilder aiRecommendation(AiRecommendation aiRecommendation) { this.aiRecommendation = aiRecommendation; return this; }

        public Complaint build() {
            Complaint c = new Complaint();
            c.setId(this.id);
            c.setComplaintNumber(this.complaintNumber);
            c.setTitle(this.title);
            c.setDescription(this.description);
            c.setCategory(this.category);
            c.setStatus(this.status != null ? this.status : ComplaintStatus.SUBMITTED);
            c.setPriority(this.priority != null ? this.priority : Priority.MEDIUM);
            c.setLocation(this.location);
            c.setLatitude(this.latitude);
            c.setLongitude(this.longitude);
            c.setImageUrl(this.imageUrl);
            c.setCitizen(this.citizen);
            c.setDepartment(this.department);
            c.setAssignedTo(this.assignedTo);
            c.setRejectionReason(this.rejectionReason);
            c.setResolutionNotes(this.resolutionNotes);
            c.setResolutionImageUrl(this.resolutionImageUrl);
            c.setAdminNotes(this.adminNotes);
            c.setNeedsInfoRemarks(this.needsInfoRemarks);
            c.setAiCategoryOverridden(this.aiCategoryOverridden != null ? this.aiCategoryOverridden : false);
            c.setAiPriorityOverridden(this.aiPriorityOverridden != null ? this.aiPriorityOverridden : false);
            c.setStartedWorkingAt(this.startedWorkingAt);
            c.setProgressNotes(this.progressNotes);
            c.setCreatedAt(this.createdAt);
            c.setUpdatedAt(this.updatedAt);
            c.setResolvedAt(this.resolvedAt);
            c.setClosedAt(this.closedAt);
            c.setClosedBy(this.closedBy);
            c.setClosureRemarks(this.closureRemarks);
            c.setCitizenConfirmedResolution(this.citizenConfirmedResolution != null ? this.citizenConfirmedResolution : false);
            c.setFeedback(this.feedback);
            c.setAiRecommendation(this.aiRecommendation);
            return c;
        }
    }
}
