package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public class RequestInfoRequest {

    @NotBlank(message = "Information request message is mandatory")
    private String remarks;

    public RequestInfoRequest() {}

    public RequestInfoRequest(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
