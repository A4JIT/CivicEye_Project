package com.civiceye.ai.complaint;

import com.civiceye.ai.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_status_history")
public class ComplaintStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ComplaintStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private ComplaintStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "resolution_image_url", length = 500)
    private String resolutionImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ComplaintStatusHistory() {
    }

    public ComplaintStatusHistory(Long id, Complaint complaint, ComplaintStatus fromStatus, ComplaintStatus toStatus,
                                  User changedBy, String remarks, String resolutionImageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.complaint = complaint;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.resolutionImageUrl = resolutionImageUrl;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getResolutionImageUrl() {
        return resolutionImageUrl;
    }

    public void setResolutionImageUrl(String resolutionImageUrl) {
        this.resolutionImageUrl = resolutionImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ComplaintStatusHistoryBuilder builder() {
        return new ComplaintStatusHistoryBuilder();
    }

    public static class ComplaintStatusHistoryBuilder {
        private Long id;
        private Complaint complaint;
        private ComplaintStatus fromStatus;
        private ComplaintStatus toStatus;
        private User changedBy;
        private String remarks;
        private String resolutionImageUrl;
        private LocalDateTime createdAt;

        public ComplaintStatusHistoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ComplaintStatusHistoryBuilder complaint(Complaint complaint) {
            this.complaint = complaint;
            return this;
        }

        public ComplaintStatusHistoryBuilder fromStatus(ComplaintStatus fromStatus) {
            this.fromStatus = fromStatus;
            return this;
        }

        public ComplaintStatusHistoryBuilder toStatus(ComplaintStatus toStatus) {
            this.toStatus = toStatus;
            return this;
        }

        public ComplaintStatusHistoryBuilder changedBy(User changedBy) {
            this.changedBy = changedBy;
            return this;
        }

        public ComplaintStatusHistoryBuilder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public ComplaintStatusHistoryBuilder resolutionImageUrl(String resolutionImageUrl) {
            this.resolutionImageUrl = resolutionImageUrl;
            return this;
        }

        public ComplaintStatusHistoryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ComplaintStatusHistory build() {
            return new ComplaintStatusHistory(id, complaint, fromStatus, toStatus, changedBy, remarks, resolutionImageUrl, createdAt);
        }
    }
}
