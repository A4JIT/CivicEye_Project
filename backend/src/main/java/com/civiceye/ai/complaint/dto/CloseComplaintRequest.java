package com.civiceye.ai.complaint.dto;

public class CloseComplaintRequest {

    private String closureRemarks;

    public CloseComplaintRequest() {
    }

    public CloseComplaintRequest(String closureRemarks) {
        this.closureRemarks = closureRemarks;
    }

    public String getClosureRemarks() {
        return closureRemarks;
    }

    public void setClosureRemarks(String closureRemarks) {
        this.closureRemarks = closureRemarks;
    }
}
