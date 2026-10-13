package com.civiceye.ai.complaint;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ComplaintStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;

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
        public ComplaintBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public ComplaintBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

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
            c.setCreatedAt(this.createdAt);
            c.setUpdatedAt(this.updatedAt);
            c.setResolvedAt(this.resolvedAt);
            return c;
        }
    }
}
