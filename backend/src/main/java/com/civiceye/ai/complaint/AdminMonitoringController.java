package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.ComplaintFilterRequest;
import com.civiceye.ai.complaint.dto.ComplaintMonitoringSummaryDto;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/monitoring", "/api/admin/monitoring"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminMonitoringController {

    private final ComplaintMonitoringService monitoringService;

    public AdminMonitoringController(ComplaintMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonitoredComplaints(
            @ModelAttribute ComplaintFilterRequest filterRequest) {
        Page<ComplaintResponse> pageResult = monitoringService.filterComplaints(filterRequest);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("complaints", pageResult.getContent());
        responseData.put("currentPage", pageResult.getNumber());
        responseData.put("totalItems", pageResult.getTotalElements());
        responseData.put("totalPages", pageResult.getTotalPages());
        responseData.put("pageSize", pageResult.getSize());

        return ResponseEntity.ok(ApiResponse.success("Administrative complaint monitoring data retrieved successfully", responseData));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ComplaintMonitoringSummaryDto>> getMonitoringSummary() {
        ComplaintMonitoringSummaryDto summary = monitoringService.getMonitoringSummary();
        return ResponseEntity.ok(ApiResponse.success("Administrative monitoring summary retrieved successfully", summary));
    }
}
