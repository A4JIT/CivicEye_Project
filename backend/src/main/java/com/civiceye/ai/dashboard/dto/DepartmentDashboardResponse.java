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
}
