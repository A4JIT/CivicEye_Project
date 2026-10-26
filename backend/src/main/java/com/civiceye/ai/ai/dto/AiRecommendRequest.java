package com.civiceye.ai.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class AiRecommendRequest {

    @NotBlank(message = "Title is mandatory for AI recommendation calculation")
    private String title;

    private String description;

    private String location;

    public AiRecommendRequest() {
    }

    public AiRecommendRequest(String title, String description, String location) {
        this.title = title;
        this.description = description;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static AiRecommendRequestBuilder builder() {
        return new AiRecommendRequestBuilder();
    }

    public static class AiRecommendRequestBuilder {
        private String title;
        private String description;
        private String location;

        public AiRecommendRequestBuilder title(String title) { this.title = title; return this; }
        public AiRecommendRequestBuilder description(String description) { this.description = description; return this; }
        public AiRecommendRequestBuilder location(String location) { this.location = location; return this; }

        public AiRecommendRequest build() {
            return new AiRecommendRequest(this.title, this.description, this.location);
        }
    }
}
