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
}
