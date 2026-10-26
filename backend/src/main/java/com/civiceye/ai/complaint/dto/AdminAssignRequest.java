package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.Priority;
import jakarta.validation.constraints.NotNull;

public class AdminAssignRequest {

    @NotNull(message = "Department ID is mandatory")
    private Long departmentId;

    private Long assignedOfficerId;
    private Priority priority;
    private String instructions;

    public AdminAssignRequest() {}

    public AdminAssignRequest(Long departmentId) {
        this.departmentId = departmentId;
    }

    public AdminAssignRequest(Long departmentId, Long assignedOfficerId, Priority priority, String instructions) {
        this.departmentId = departmentId;
        this.assignedOfficerId = assignedOfficerId;
        this.priority = priority;
        this.instructions = instructions;
    }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(Long assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
