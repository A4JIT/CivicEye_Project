package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    private String remarks;

    private String resolutionImageUrl;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(ComplaintStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public StatusUpdateRequest(ComplaintStatus status, String remarks, String resolutionImageUrl) {
        this.status = status;
        this.remarks = remarks;
        this.resolutionImageUrl = resolutionImageUrl;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
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
}
