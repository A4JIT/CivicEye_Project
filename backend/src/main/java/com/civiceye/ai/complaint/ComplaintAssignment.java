package com.civiceye.ai.complaint;

import com.civiceye.ai.department.Department;
import com.civiceye.ai.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_assignments")
public class ComplaintAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public ComplaintAssignment() {
    }

    public ComplaintAssignment(Long id, Complaint complaint, Department department, User assignedTo,
                               User assignedBy, String instructions, Boolean isActive,
                               LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.complaint = complaint;
        this.department = department;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.instructions = instructions;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static ComplaintAssignmentBuilder builder() {
        return new ComplaintAssignmentBuilder();
    }

    public static class ComplaintAssignmentBuilder {
        private Long id;
        private Complaint complaint;
        private Department department;
        private User assignedTo;
        private User assignedBy;
        private String instructions;
        private Boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public ComplaintAssignmentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ComplaintAssignmentBuilder complaint(Complaint complaint) {
            this.complaint = complaint;
            return this;
        }

        public ComplaintAssignmentBuilder department(Department department) {
            this.department = department;
            return this;
        }

        public ComplaintAssignmentBuilder assignedTo(User assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public ComplaintAssignmentBuilder assignedBy(User assignedBy) {
            this.assignedBy = assignedBy;
            return this;
        }

        public ComplaintAssignmentBuilder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public ComplaintAssignmentBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public ComplaintAssignmentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ComplaintAssignmentBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public ComplaintAssignment build() {
            return new ComplaintAssignment(id, complaint, department, assignedTo, assignedBy, instructions, isActive, createdAt, completedAt);
        }
    }
}
