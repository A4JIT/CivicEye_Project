package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotNull;

public class AssignComplaintRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long assignedOfficerId;

    private String instructions;

    public AssignComplaintRequest() {
    }

    public AssignComplaintRequest(Long departmentId) {
        this.departmentId = departmentId;
    }

    public AssignComplaintRequest(Long departmentId, Long assignedOfficerId, String instructions) {
        this.departmentId = departmentId;
        this.assignedOfficerId = assignedOfficerId;
        this.instructions = instructions;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
