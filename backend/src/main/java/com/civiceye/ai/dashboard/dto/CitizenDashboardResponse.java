package com.civiceye.ai.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenDashboardResponse {

    private long totalComplaints;
    private long submittedCount;
    private long underReviewCount;
    private long inProgressCount;
    private long resolvedCount;
    private long rejectedCount;
    private List<ComplaintSummaryDto> recentComplaints;

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }
    public long getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(long submittedCount) { this.submittedCount = submittedCount; }
    public long getUnderReviewCount() { return underReviewCount; }
    public void setUnderReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; }
    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
    public List<ComplaintSummaryDto> getRecentComplaints() { return recentComplaints; }
    public void setRecentComplaints(List<ComplaintSummaryDto> recentComplaints) { this.recentComplaints = recentComplaints; }

    public static CitizenDashboardResponseBuilder builder() {
        return new CitizenDashboardResponseBuilder();
    }

    public static class CitizenDashboardResponseBuilder {
        private long totalComplaints;
        private long submittedCount;
        private long underReviewCount;
        private long inProgressCount;
        private long resolvedCount;
        private long rejectedCount;
        private List<ComplaintSummaryDto> recentComplaints;

        public CitizenDashboardResponseBuilder totalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; return this; }
        public CitizenDashboardResponseBuilder submittedCount(long submittedCount) { this.submittedCount = submittedCount; return this; }
        public CitizenDashboardResponseBuilder underReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; return this; }
        public CitizenDashboardResponseBuilder inProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; return this; }
        public CitizenDashboardResponseBuilder resolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; return this; }
        public CitizenDashboardResponseBuilder rejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; return this; }
        public CitizenDashboardResponseBuilder recentComplaints(List<ComplaintSummaryDto> recentComplaints) { this.recentComplaints = recentComplaints; return this; }

        public CitizenDashboardResponse build() {
            CitizenDashboardResponse res = new CitizenDashboardResponse();
            res.setTotalComplaints(this.totalComplaints);
            res.setSubmittedCount(this.submittedCount);
            res.setUnderReviewCount(this.underReviewCount);
            res.setInProgressCount(this.inProgressCount);
            res.setResolvedCount(this.resolvedCount);
            res.setRejectedCount(this.rejectedCount);
            res.setRecentComplaints(this.recentComplaints);
            return res;
        }
    }
}
