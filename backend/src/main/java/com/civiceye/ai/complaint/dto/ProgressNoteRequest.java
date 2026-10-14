package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public class ProgressNoteRequest {

    @NotBlank(message = "Progress note cannot be empty")
    private String notes;

    public ProgressNoteRequest() {
    }

    public ProgressNoteRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
