package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectComplaintRequest {

    @NotBlank(message = "Rejection reason is mandatory")
    private String rejectionReason;

    private String rejectionCategory;

    public RejectComplaintRequest() {}

    public RejectComplaintRequest(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public RejectComplaintRequest(String rejectionReason, String rejectionCategory) {
        this.rejectionReason = rejectionReason;
        this.rejectionCategory = rejectionCategory;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getRejectionCategory() { return rejectionCategory; }
    public void setRejectionCategory(String rejectionCategory) { this.rejectionCategory = rejectionCategory; }
}
