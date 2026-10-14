package com.civiceye.ai.complaint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitFeedbackRequest {

    @NotNull(message = "Satisfaction rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comments;

    public SubmitFeedbackRequest() {
    }

    public SubmitFeedbackRequest(Integer rating, String comments) {
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

    public static SubmitFeedbackRequestBuilder builder() {
        return new SubmitFeedbackRequestBuilder();
    }

    public static class SubmitFeedbackRequestBuilder {
        private Integer rating;
        private String comments;

        public SubmitFeedbackRequestBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public SubmitFeedbackRequestBuilder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public SubmitFeedbackRequest build() {
            return new SubmitFeedbackRequest(rating, comments);
        }
    }
}
