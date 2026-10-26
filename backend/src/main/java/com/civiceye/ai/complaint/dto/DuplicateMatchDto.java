package com.civiceye.ai.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateMatchDto {
    private Long complaintId;
    private String complaintNumber;
    private String title;
    private String description;
    private String category;
    private String status;
    private String priority;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double distanceMeters;
    private Double similarityScore;
    private Integer upvotesCount;
    private LocalDateTime createdAt;
    private List<String> matchReasons;

    public DuplicateMatchDto() {
    }

    public DuplicateMatchDto(Long complaintId, String complaintNumber, String title, String description,
                             String category, String status, String priority, String location,
                             Double latitude, Double longitude, Double distanceMeters,
                             Double similarityScore, Integer upvotesCount, LocalDateTime createdAt,
                             List<String> matchReasons) {
        this.complaintId = complaintId;
        this.complaintNumber = complaintNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = distanceMeters;
        this.similarityScore = similarityScore;
        this.upvotesCount = upvotesCount;
        this.createdAt = createdAt;
        this.matchReasons = matchReasons;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Integer getUpvotesCount() {
        return upvotesCount;
    }

    public void setUpvotesCount(Integer upvotesCount) {
        this.upvotesCount = upvotesCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("matchReason")
    public String getMatchReason() {
        if (matchReasons != null && !matchReasons.isEmpty()) {
            return String.join(" • ", matchReasons);
        }
        return "High similarity detected with an existing active complaint.";
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    public static DuplicateMatchDtoBuilder builder() {
        return new DuplicateMatchDtoBuilder();
    }

    public static class DuplicateMatchDtoBuilder {
        private Long complaintId;
        private String complaintNumber;
        private String title;
        private String description;
        private String category;
        private String status;
        private String priority;
        private String location;
        private Double latitude;
        private Double longitude;
        private Double distanceMeters;
        private Double similarityScore;
        private Integer upvotesCount;
        private LocalDateTime createdAt;
        private List<String> matchReasons;

        public DuplicateMatchDtoBuilder complaintId(Long complaintId) { this.complaintId = complaintId; return this; }
        public DuplicateMatchDtoBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public DuplicateMatchDtoBuilder title(String title) { this.title = title; return this; }
        public DuplicateMatchDtoBuilder description(String description) { this.description = description; return this; }
        public DuplicateMatchDtoBuilder category(String category) { this.category = category; return this; }
        public DuplicateMatchDtoBuilder status(String status) { this.status = status; return this; }
        public DuplicateMatchDtoBuilder priority(String priority) { this.priority = priority; return this; }
        public DuplicateMatchDtoBuilder location(String location) { this.location = location; return this; }
        public DuplicateMatchDtoBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public DuplicateMatchDtoBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public DuplicateMatchDtoBuilder distanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; return this; }
        public DuplicateMatchDtoBuilder similarityScore(Double similarityScore) { this.similarityScore = similarityScore; return this; }
        public DuplicateMatchDtoBuilder upvotesCount(Integer upvotesCount) { this.upvotesCount = upvotesCount; return this; }
        public DuplicateMatchDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public DuplicateMatchDtoBuilder matchReasons(List<String> matchReasons) { this.matchReasons = matchReasons; return this; }

        public DuplicateMatchDto build() {
            return new DuplicateMatchDto(complaintId, complaintNumber, title, description, category,
                    status, priority, location, latitude, longitude, distanceMeters,
                    similarityScore, upvotesCount, createdAt, matchReasons);
        }
    }
}
