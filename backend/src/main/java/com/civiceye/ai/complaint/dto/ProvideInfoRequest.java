package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public class ProvideInfoRequest {

    @NotBlank(message = "Information response message is mandatory")
    private String remarks;

    private String additionalImageUrl;

    public ProvideInfoRequest() {}

    public ProvideInfoRequest(String remarks) {
        this.remarks = remarks;
    }

    public ProvideInfoRequest(String remarks, String additionalImageUrl) {
        this.remarks = remarks;
        this.additionalImageUrl = additionalImageUrl;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getAdditionalImageUrl() { return additionalImageUrl; }
    public void setAdditionalImageUrl(String additionalImageUrl) { this.additionalImageUrl = additionalImageUrl; }
}
