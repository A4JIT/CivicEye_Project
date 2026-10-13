package com.civiceye.ai.complaint;

import com.civiceye.ai.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_feedback")
public class ComplaintFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ComplaintFeedback() {
    }

    public ComplaintFeedback(Long id, Complaint complaint, User citizen, Integer rating, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.complaint = complaint;
        this.citizen = citizen;
        this.rating = rating;
        this.comments = comments;
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

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ComplaintFeedbackBuilder builder() {
        return new ComplaintFeedbackBuilder();
    }

    public static class ComplaintFeedbackBuilder {
        private Long id;
        private Complaint complaint;
        private User citizen;
        private Integer rating;
        private String comments;
        private LocalDateTime createdAt;

        public ComplaintFeedbackBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ComplaintFeedbackBuilder complaint(Complaint complaint) {
            this.complaint = complaint;
            return this;
        }

        public ComplaintFeedbackBuilder citizen(User citizen) {
            this.citizen = citizen;
            return this;
        }

        public ComplaintFeedbackBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public ComplaintFeedbackBuilder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public ComplaintFeedbackBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ComplaintFeedback build() {
            return new ComplaintFeedback(id, complaint, citizen, rating, comments, createdAt);
        }
    }
}
