package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ConfirmResolutionRequest {

    private String comments;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    public ConfirmResolutionRequest() {
    }

    public ConfirmResolutionRequest(String comments) {
        this.comments = comments;
    }

    public ConfirmResolutionRequest(String comments, Integer rating) {
        this.comments = comments;
        this.rating = rating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
