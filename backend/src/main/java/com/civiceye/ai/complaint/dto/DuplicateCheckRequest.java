package com.civiceye.ai.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckRequest {
    private String title;
    private String description;
    private String category;
    private String location;
    private Double latitude;
    private Double longitude;

    public DuplicateCheckRequest(String title, String description, String category, String location, Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DuplicateCheckRequest() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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

    public static DuplicateCheckRequestBuilder builder() {
        return new DuplicateCheckRequestBuilder();
    }

    public static class DuplicateCheckRequestBuilder {
        private String title;
        private String description;
        private String category;
        private String location;
        private Double latitude;
        private Double longitude;

        public DuplicateCheckRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public DuplicateCheckRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public DuplicateCheckRequestBuilder category(String category) {
            this.category = category;
            return this;
        }

        public DuplicateCheckRequestBuilder location(String location) {
            this.location = location;
            return this;
        }

        public DuplicateCheckRequestBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public DuplicateCheckRequestBuilder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public DuplicateCheckRequest build() {
            return new DuplicateCheckRequest(title, description, category, location, latitude, longitude);
        }
    }
}
