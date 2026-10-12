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
}
