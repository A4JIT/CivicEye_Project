package com.civiceye.ai.complaint;

import com.civiceye.ai.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "complaint_upvotes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_complaint_citizen_upvote", columnNames = {"complaint_id", "citizen_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ComplaintUpvote() {
    }

    public ComplaintUpvote(Long id, Complaint complaint, User citizen, LocalDateTime createdAt) {
        this.id = id;
        this.complaint = complaint;
        this.citizen = citizen;
        this.createdAt = createdAt;
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

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static ComplaintUpvoteBuilder builder() {
        return new ComplaintUpvoteBuilder();
    }

    public static class ComplaintUpvoteBuilder {
        private Long id;
        private Complaint complaint;
        private User citizen;
        private LocalDateTime createdAt;

        public ComplaintUpvoteBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ComplaintUpvoteBuilder complaint(Complaint complaint) {
            this.complaint = complaint;
            return this;
        }

        public ComplaintUpvoteBuilder citizen(User citizen) {
            this.citizen = citizen;
            return this;
        }

        public ComplaintUpvoteBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ComplaintUpvote build() {
            return new ComplaintUpvote(id, complaint, citizen, createdAt);
        }
    }
}
