package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateComplaintRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @NotBlank(message = "Location address or landmark is required")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private Double latitude;

    private Double longitude;

    public CreateComplaintRequest() {
    }

    public CreateComplaintRequest(String title, String description, ComplaintCategory category, String location, Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public static CreateComplaintRequestBuilder builder() {
        return new CreateComplaintRequestBuilder();
    }

    public static class CreateComplaintRequestBuilder {
        private String title;
        private String description;
        private ComplaintCategory category;
        private String location;
        private Double latitude;
        private Double longitude;

        public CreateComplaintRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public CreateComplaintRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateComplaintRequestBuilder category(ComplaintCategory category) {
            this.category = category;
            return this;
        }

        public CreateComplaintRequestBuilder location(String location) {
            this.location = location;
            return this;
        }

        public CreateComplaintRequestBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public CreateComplaintRequestBuilder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public CreateComplaintRequest build() {
            return new CreateComplaintRequest(title, description, category, location, latitude, longitude);
        }
    }
}
