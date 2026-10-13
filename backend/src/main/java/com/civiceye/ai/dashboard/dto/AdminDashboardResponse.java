package com.civiceye.ai.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalComplaints;
    private long pendingReviewCount;
    private long inProgressCount;
    private long resolvedCount;
    private long rejectedCount;
    private double resolutionRatePercentage;
    private Map<String, Long> complaintsByCategory;
    private Map<String, Long> complaintsByDepartment;
    private List<ComplaintSummaryDto> recentComplaints;

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }
    public long getPendingReviewCount() { return pendingReviewCount; }
    public void setPendingReviewCount(long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; }
    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
    public double getResolutionRatePercentage() { return resolutionRatePercentage; }
    public void setResolutionRatePercentage(double resolutionRatePercentage) { this.resolutionRatePercentage = resolutionRatePercentage; }
    public Map<String, Long> getComplaintsByCategory() { return complaintsByCategory; }
    public void setComplaintsByCategory(Map<String, Long> complaintsByCategory) { this.complaintsByCategory = complaintsByCategory; }
    public Map<String, Long> getComplaintsByDepartment() { return complaintsByDepartment; }
    public void setComplaintsByDepartment(Map<String, Long> complaintsByDepartment) { this.complaintsByDepartment = complaintsByDepartment; }
    public List<ComplaintSummaryDto> getRecentComplaints() { return recentComplaints; }
    public void setRecentComplaints(List<ComplaintSummaryDto> recentComplaints) { this.recentComplaints = recentComplaints; }

    public static AdminDashboardResponseBuilder builder() {
        return new AdminDashboardResponseBuilder();
    }

    public static class AdminDashboardResponseBuilder {
        private long totalComplaints;
        private long pendingReviewCount;
        private long inProgressCount;
        private long resolvedCount;
        private long rejectedCount;
        private double resolutionRatePercentage;
        private Map<String, Long> complaintsByCategory;
        private Map<String, Long> complaintsByDepartment;
        private List<ComplaintSummaryDto> recentComplaints;

        public AdminDashboardResponseBuilder totalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; return this; }
        public AdminDashboardResponseBuilder pendingReviewCount(long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; return this; }
        public AdminDashboardResponseBuilder inProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; return this; }
        public AdminDashboardResponseBuilder resolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; return this; }
        public AdminDashboardResponseBuilder rejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; return this; }
        public AdminDashboardResponseBuilder resolutionRatePercentage(double resolutionRatePercentage) { this.resolutionRatePercentage = resolutionRatePercentage; return this; }
        public AdminDashboardResponseBuilder complaintsByCategory(Map<String, Long> complaintsByCategory) { this.complaintsByCategory = complaintsByCategory; return this; }
        public AdminDashboardResponseBuilder complaintsByDepartment(Map<String, Long> complaintsByDepartment) { this.complaintsByDepartment = complaintsByDepartment; return this; }
        public AdminDashboardResponseBuilder recentComplaints(List<ComplaintSummaryDto> recentComplaints) { this.recentComplaints = recentComplaints; return this; }

        public AdminDashboardResponse build() {
            AdminDashboardResponse res = new AdminDashboardResponse();
            res.setTotalComplaints(this.totalComplaints);
            res.setPendingReviewCount(this.pendingReviewCount);
            res.setInProgressCount(this.inProgressCount);
            res.setResolvedCount(this.resolvedCount);
            res.setRejectedCount(this.rejectedCount);
            res.setResolutionRatePercentage(this.resolutionRatePercentage);
            res.setComplaintsByCategory(this.complaintsByCategory);
            res.setComplaintsByDepartment(this.complaintsByDepartment);
            res.setRecentComplaints(this.recentComplaints);
            return res;
        }
    }
}
