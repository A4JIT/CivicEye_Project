package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ComplaintFeedbackRequest {

    @NotNull(message = "Satisfaction rating is required")
    @Min(value = 1, message = "Satisfaction rating must be at least 1")
    @Max(value = 5, message = "Satisfaction rating cannot exceed 5")
    private Integer rating;

    private String comments;

    public ComplaintFeedbackRequest() {
    }

    public ComplaintFeedbackRequest(Integer rating, String comments) {
        this.rating = rating;
        this.comments = comments;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
