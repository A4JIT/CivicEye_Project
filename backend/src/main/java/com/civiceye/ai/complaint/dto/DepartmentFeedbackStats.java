package com.civiceye.ai.complaint.dto;

public class DepartmentFeedbackStats {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Double averageRating;
    private Long totalReviews;

    public DepartmentFeedbackStats() {
    }

    public DepartmentFeedbackStats(Long departmentId, String departmentName, String departmentCode, Double averageRating, Long totalReviews) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public static DepartmentFeedbackStatsBuilder builder() {
        return new DepartmentFeedbackStatsBuilder();
    }

    public static class DepartmentFeedbackStatsBuilder {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private Double averageRating;
        private Long totalReviews;

        public DepartmentFeedbackStatsBuilder departmentId(Long departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public DepartmentFeedbackStatsBuilder departmentName(String departmentName) {
            this.departmentName = departmentName;
            return this;
        }

        public DepartmentFeedbackStatsBuilder departmentCode(String departmentCode) {
            this.departmentCode = departmentCode;
            return this;
        }

        public DepartmentFeedbackStatsBuilder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public DepartmentFeedbackStatsBuilder totalReviews(Long totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        public DepartmentFeedbackStats build() {
            return new DepartmentFeedbackStats(departmentId, departmentName, departmentCode, averageRating, totalReviews);
        }
    }
}
