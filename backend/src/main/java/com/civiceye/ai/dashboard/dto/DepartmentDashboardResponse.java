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
public class DepartmentDashboardResponse {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private long totalAssignedComplaints;
    private long pendingActionCount;
    private long inProgressCount;
    private long resolvedCount;
    private long criticalAlertsCount;
    private List<ComplaintSummaryDto> recentAssignedComplaints;

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public long getTotalAssignedComplaints() { return totalAssignedComplaints; }
    public void setTotalAssignedComplaints(long totalAssignedComplaints) { this.totalAssignedComplaints = totalAssignedComplaints; }
    public long getPendingActionCount() { return pendingActionCount; }
    public void setPendingActionCount(long pendingActionCount) { this.pendingActionCount = pendingActionCount; }
    public long getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; }
    public long getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; }
    public long getCriticalAlertsCount() { return criticalAlertsCount; }
    public void setCriticalAlertsCount(long criticalAlertsCount) { this.criticalAlertsCount = criticalAlertsCount; }
    public List<ComplaintSummaryDto> getRecentAssignedComplaints() { return recentAssignedComplaints; }
    public void setRecentAssignedComplaints(List<ComplaintSummaryDto> recentAssignedComplaints) { this.recentAssignedComplaints = recentAssignedComplaints; }

    public static DepartmentDashboardResponseBuilder builder() {
        return new DepartmentDashboardResponseBuilder();
    }

    public static class DepartmentDashboardResponseBuilder {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private long totalAssignedComplaints;
        private long pendingActionCount;
        private long inProgressCount;
        private long resolvedCount;
        private long criticalAlertsCount;
        private List<ComplaintSummaryDto> recentAssignedComplaints;

        public DepartmentDashboardResponseBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public DepartmentDashboardResponseBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public DepartmentDashboardResponseBuilder departmentCode(String departmentCode) { this.departmentCode = departmentCode; return this; }
        public DepartmentDashboardResponseBuilder totalAssignedComplaints(long totalAssignedComplaints) { this.totalAssignedComplaints = totalAssignedComplaints; return this; }
        public DepartmentDashboardResponseBuilder pendingActionCount(long pendingActionCount) { this.pendingActionCount = pendingActionCount; return this; }
        public DepartmentDashboardResponseBuilder inProgressCount(long inProgressCount) { this.inProgressCount = inProgressCount; return this; }
        public DepartmentDashboardResponseBuilder resolvedCount(long resolvedCount) { this.resolvedCount = resolvedCount; return this; }
        public DepartmentDashboardResponseBuilder criticalAlertsCount(long criticalAlertsCount) { this.criticalAlertsCount = criticalAlertsCount; return this; }
        public DepartmentDashboardResponseBuilder recentAssignedComplaints(List<ComplaintSummaryDto> recentAssignedComplaints) { this.recentAssignedComplaints = recentAssignedComplaints; return this; }

        public DepartmentDashboardResponse build() {
            DepartmentDashboardResponse res = new DepartmentDashboardResponse();
            res.setDepartmentId(this.departmentId);
            res.setDepartmentName(this.departmentName);
            res.setDepartmentCode(this.departmentCode);
            res.setTotalAssignedComplaints(this.totalAssignedComplaints);
            res.setPendingActionCount(this.pendingActionCount);
            res.setInProgressCount(this.inProgressCount);
            res.setResolvedCount(this.resolvedCount);
            res.setCriticalAlertsCount(this.criticalAlertsCount);
            res.setRecentAssignedComplaints(this.recentAssignedComplaints);
            return res;
        }
    }
}
