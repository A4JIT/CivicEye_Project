package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintAssignment;

import java.time.LocalDateTime;

public class ComplaintAssignmentResponse {

    private Long id;
    private Long complaintId;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long assignedToId;
    private String assignedToName;
    private Long assignedById;
    private String assignedByName;
    private String instructions;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ComplaintAssignmentResponse() {
    }

    public ComplaintAssignmentResponse(Long id, Long complaintId, Long departmentId, String departmentName,
                                       String departmentCode, Long assignedToId, String assignedToName,
                                       Long assignedById, String assignedByName, String instructions,
                                       Boolean isActive, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignedById = assignedById;
        this.assignedByName = assignedByName;
        this.instructions = instructions;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static ComplaintAssignmentResponse fromEntity(ComplaintAssignment assignment) {
        if (assignment == null) return null;
        return ComplaintAssignmentResponse.builder()
                .id(assignment.getId())
                .complaintId(assignment.getComplaint() != null ? assignment.getComplaint().getId() : null)
                .departmentId(assignment.getDepartment() != null ? assignment.getDepartment().getId() : null)
                .departmentName(assignment.getDepartment() != null ? assignment.getDepartment().getName() : null)
                .departmentCode(assignment.getDepartment() != null ? assignment.getDepartment().getCode() : null)
                .assignedToId(assignment.getAssignedTo() != null ? assignment.getAssignedTo().getId() : null)
                .assignedToName(assignment.getAssignedTo() != null ? assignment.getAssignedTo().getFullName() : null)
                .assignedById(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null)
                .assignedByName(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getFullName() : null)
                .instructions(assignment.getInstructions())
                .isActive(assignment.getIsActive())
                .createdAt(assignment.getCreatedAt())
                .completedAt(assignment.getCompletedAt())
                .build();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public Long getAssignedById() { return assignedById; }
    public void setAssignedById(Long assignedById) { this.assignedById = assignedById; }

    public String getAssignedByName() { return assignedByName; }
    public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public static ComplaintAssignmentResponseBuilder builder() {
        return new ComplaintAssignmentResponseBuilder();
    }

    public static class ComplaintAssignmentResponseBuilder {
        private Long id;
        private Long complaintId;
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private Long assignedToId;
        private String assignedToName;
        private Long assignedById;
        private String assignedByName;
        private String instructions;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public ComplaintAssignmentResponseBuilder id(Long id) { this.id = id; return this; }
        public ComplaintAssignmentResponseBuilder complaintId(Long complaintId) { this.complaintId = complaintId; return this; }
        public ComplaintAssignmentResponseBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public ComplaintAssignmentResponseBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public ComplaintAssignmentResponseBuilder departmentCode(String departmentCode) { this.departmentCode = departmentCode; return this; }
        public ComplaintAssignmentResponseBuilder assignedToId(Long assignedToId) { this.assignedToId = assignedToId; return this; }
        public ComplaintAssignmentResponseBuilder assignedToName(String assignedToName) { this.assignedToName = assignedToName; return this; }
        public ComplaintAssignmentResponseBuilder assignedById(Long assignedById) { this.assignedById = assignedById; return this; }
        public ComplaintAssignmentResponseBuilder assignedByName(String assignedByName) { this.assignedByName = assignedByName; return this; }
        public ComplaintAssignmentResponseBuilder instructions(String instructions) { this.instructions = instructions; return this; }
        public ComplaintAssignmentResponseBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public ComplaintAssignmentResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintAssignmentResponseBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public ComplaintAssignmentResponse build() {
            return new ComplaintAssignmentResponse(id, complaintId, departmentId, departmentName, departmentCode,
                    assignedToId, assignedToName, assignedById, assignedByName, instructions, isActive, createdAt, completedAt);
        }
    }
}
