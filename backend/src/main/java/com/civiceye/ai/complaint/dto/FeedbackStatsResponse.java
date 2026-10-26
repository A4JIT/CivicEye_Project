package com.civiceye.ai.complaint.dto;

import java.util.List;
import java.util.Map;

public class FeedbackStatsResponse {

    private Double overallAverageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution;
    private List<DepartmentFeedbackStats> departmentStats;

    public FeedbackStatsResponse() {
    }

    public FeedbackStatsResponse(Double overallAverageRating, Long totalReviews, Map<Integer, Long> ratingDistribution, List<DepartmentFeedbackStats> departmentStats) {
        this.overallAverageRating = overallAverageRating;
        this.totalReviews = totalReviews;
        this.ratingDistribution = ratingDistribution;
        this.departmentStats = departmentStats;
    }

    public Double getOverallAverageRating() {
        return overallAverageRating;
    }

    public void setOverallAverageRating(Double overallAverageRating) {
        this.overallAverageRating = overallAverageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public List<DepartmentFeedbackStats> getDepartmentStats() {
        return departmentStats;
    }

    public void setDepartmentStats(List<DepartmentFeedbackStats> departmentStats) {
        this.departmentStats = departmentStats;
    }

    public static FeedbackStatsResponseBuilder builder() {
        return new FeedbackStatsResponseBuilder();
    }

    public static class FeedbackStatsResponseBuilder {
        private Double overallAverageRating;
        private Long totalReviews;
        private Map<Integer, Long> ratingDistribution;
        private List<DepartmentFeedbackStats> departmentStats;

        public FeedbackStatsResponseBuilder overallAverageRating(Double overallAverageRating) {
            this.overallAverageRating = overallAverageRating;
            return this;
        }

        public FeedbackStatsResponseBuilder totalReviews(Long totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        public FeedbackStatsResponseBuilder ratingDistribution(Map<Integer, Long> ratingDistribution) {
            this.ratingDistribution = ratingDistribution;
            return this;
        }

        public FeedbackStatsResponseBuilder departmentStats(List<DepartmentFeedbackStats> departmentStats) {
            this.departmentStats = departmentStats;
            return this;
        }

        public FeedbackStatsResponse build() {
            return new FeedbackStatsResponse(overallAverageRating, totalReviews, ratingDistribution, departmentStats);
        }
    }
}
