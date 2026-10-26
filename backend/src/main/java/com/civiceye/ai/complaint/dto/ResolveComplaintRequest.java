package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveComplaintRequest {

    @NotBlank(message = "Resolution notes are mandatory and cannot be empty")
    private String resolutionNotes;

    private String resolutionImageUrl;

    public ResolveComplaintRequest() {
    }

    public ResolveComplaintRequest(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public ResolveComplaintRequest(String resolutionNotes, String resolutionImageUrl) {
        this.resolutionNotes = resolutionNotes;
        this.resolutionImageUrl = resolutionImageUrl;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getResolutionImageUrl() {
        return resolutionImageUrl;
    }

    public void setResolutionImageUrl(String resolutionImageUrl) {
        this.resolutionImageUrl = resolutionImageUrl;
    }
}
