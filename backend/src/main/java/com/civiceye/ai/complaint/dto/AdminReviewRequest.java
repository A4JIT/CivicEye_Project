package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.Priority;

public class AdminReviewRequest {

    private String notes;
    private ComplaintCategory category;
    private Priority priority;

    public AdminReviewRequest() {}

    public AdminReviewRequest(String notes, ComplaintCategory category, Priority priority) {
        this.notes = notes;
        this.category = category;
        this.priority = priority;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}
