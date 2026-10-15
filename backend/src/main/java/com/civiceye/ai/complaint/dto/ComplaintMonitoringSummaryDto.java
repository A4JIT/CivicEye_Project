package com.civiceye.ai.complaint.dto;

import java.util.HashMap;
import java.util.Map;

public class ComplaintMonitoringSummaryDto {

    private long totalComplaints;
    private long pendingReviewCount;
    private long underReviewCount;
    private long needsInfoCount;
    private long assignedCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;
    private long rejectedCount;
    private long criticalPendingCount;
    private double resolutionRatePercentage;

    private Map<String, Long> statusBreakdown = new HashMap<>();
    private Map<String, Long> departmentBreakdown = new HashMap<>();
    private Map<String, Long> categoryBreakdown = new HashMap<>();
    private Map<String, Long> priorityBreakdown = new HashMap<>();

    public ComplaintMonitoringSummaryDto() {
    }

    public ComplaintMonitoringSummaryDto(long totalComplaints, long pendingReviewCount, long underReviewCount,
                                         long needsInfoCount, long assignedCount, long inProgressCount,
                                         long resolvedCount, long closedCount, long rejectedCount,
                                         long criticalPendingCount, double resolutionRatePercentage,
                                         Map<String, Long> statusBreakdown, Map<String, Long> departmentBreakdown,
                                         Map<String, Long> categoryBreakdown, Map<String, Long> priorityBreakdown) {
        this.totalComplaints = totalComplaints;
        this.pendingReviewCount = pendingReviewCount;
        this.underReviewCount = underReviewCount;
        this.needsInfoCount = needsInfoCount;
        this.assignedCount = assignedCount;
        this.inProgressCount = inProgressCount;
        this.resolvedCount = resolvedCount;
        this.closedCount = closedCount;
        this.rejectedCount = rejectedCount;
        this.criticalPendingCount = criticalPendingCount;
        this.resolutionRatePercentage = resolutionRatePercentage;
        this.statusBreakdown = statusBreakdown != null ? statusBreakdown : new HashMap<>();
        this.departmentBreakdown = departmentBreakdown != null ? departmentBreakdown : new HashMap<>();
        this.categoryBreakdown = categoryBreakdown != null ? categoryBreakdown : new HashMap<>();
        this.priorityBreakdown = priorityBreakdown != null ? priorityBreakdown : new HashMap<>();
    }

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    public long getPendingReviewCount() { return pendingReviewCount; }
    public void setPendingReviewCount(long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; }

    public long getUnderReviewCount() { return underReviewCount; }
    public void setUnderReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; }

    public long getNeedsInfoCount() { return needsInfoCount; }
    public void setNeedsInfoCount(long needsInfoCount) { this.needsInfoCount = needsInfoCount; }

    public long getAssignedCount() { return assignedCount; }
    public void setAssignedCount(long assignedCount) { this.assignedCount = assignedCount; }

    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }

    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }

    public long getClosedCount() { return closedCount; }
    public void setClosedCount(long closedCount) { this.closedCount = closedCount; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }

    public long getCriticalPendingCount() { return criticalPendingCount; }
    public void setCriticalPendingCount(long criticalPendingCount) { this.criticalPendingCount = criticalPendingCount; }

    public double getResolutionRatePercentage() { return resolutionRatePercentage; }
    public void setResolutionRatePercentage(double resolutionRatePercentage) { this.resolutionRatePercentage = resolutionRatePercentage; }

    public Map<String, Long> getStatusBreakdown() { return statusBreakdown; }
    public void setStatusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; }

    public Map<String, Long> getDepartmentBreakdown() { return departmentBreakdown; }
    public void setDepartmentBreakdown(Map<String, Long> departmentBreakdown) { this.departmentBreakdown = departmentBreakdown; }

    public Map<String, Long> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public Map<String, Long> getPriorityBreakdown() { return priorityBreakdown; }
    public void setPriorityBreakdown(Map<String, Long> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; }

    public static ComplaintMonitoringSummaryDtoBuilder builder() {
        return new ComplaintMonitoringSummaryDtoBuilder();
    }

    public static class ComplaintMonitoringSummaryDtoBuilder {
        private long totalComplaints;
        private long pendingReviewCount;
        private long underReviewCount;
        private long needsInfoCount;
        private long assignedCount;
        private long inProgressCount;
        private long resolvedCount;
        private long closedCount;
        private long rejectedCount;
        private long criticalPendingCount;
        private double resolutionRatePercentage;
        private Map<String, Long> statusBreakdown = new HashMap<>();
        private Map<String, Long> departmentBreakdown = new HashMap<>();
        private Map<String, Long> categoryBreakdown = new HashMap<>();
        private Map<String, Long> priorityBreakdown = new HashMap<>();

        public ComplaintMonitoringSummaryDtoBuilder totalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; return this; }
        public ComplaintMonitoringSummaryDtoBuilder pendingReviewCount(long pendingReviewCount) { this.pendingReviewCount = pendingReviewCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder underReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder needsInfoCount(long needsInfoCount) { this.needsInfoCount = needsInfoCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder assignedCount(long assignedCount) { this.assignedCount = assignedCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder inProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder resolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder closedCount(long closedCount) { this.closedCount = closedCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder rejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder criticalPendingCount(long criticalPendingCount) { this.criticalPendingCount = criticalPendingCount; return this; }
        public ComplaintMonitoringSummaryDtoBuilder resolutionRatePercentage(double resolutionRatePercentage) { this.resolutionRatePercentage = resolutionRatePercentage; return this; }
        public ComplaintMonitoringSummaryDtoBuilder statusBreakdown(Map<String, Long> statusBreakdown) { this.statusBreakdown = statusBreakdown; return this; }
        public ComplaintMonitoringSummaryDtoBuilder departmentBreakdown(Map<String, Long> departmentBreakdown) { this.departmentBreakdown = departmentBreakdown; return this; }
        public ComplaintMonitoringSummaryDtoBuilder categoryBreakdown(Map<String, Long> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; return this; }
        public ComplaintMonitoringSummaryDtoBuilder priorityBreakdown(Map<String, Long> priorityBreakdown) { this.priorityBreakdown = priorityBreakdown; return this; }

        public ComplaintMonitoringSummaryDto build() {
            return new ComplaintMonitoringSummaryDto(totalComplaints, pendingReviewCount, underReviewCount,
                    needsInfoCount, assignedCount, inProgressCount, resolvedCount, closedCount, rejectedCount,
                    criticalPendingCount, resolutionRatePercentage, statusBreakdown, departmentBreakdown, categoryBreakdown, priorityBreakdown);
        }
    }
}
