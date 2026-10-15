package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintFilterRequest;
import com.civiceye.ai.complaint.dto.ComplaintMonitoringSummaryDto;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminMonitoringControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplaintMonitoringService monitoringService;

    @InjectMocks
    private AdminMonitoringController adminMonitoringController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminMonitoringController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/monitoring/complaints returns 200 with paginated filtered complaint data")
    void getMonitoredComplaints_Returns200() throws Exception {
        ComplaintResponse sample = new ComplaintResponse();
        sample.setId(201L);
        sample.setComplaintNumber("CE-MON-201");
        sample.setTitle("Broken Water Pipe");
        sample.setStatus(ComplaintStatus.IN_PROGRESS);

        Page<ComplaintResponse> page = new PageImpl<>(List.of(sample));
        when(monitoringService.filterComplaints(any(ComplaintFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/monitoring/complaints")
                        .param("status", "IN_PROGRESS")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaints").isArray())
                .andExpect(jsonPath("$.data.complaints[0].complaintNumber").value("CE-MON-201"))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/admin/monitoring/summary returns 200 with municipal KPI aggregations")
    void getMonitoringSummary_Returns200() throws Exception {
        ComplaintMonitoringSummaryDto summary = ComplaintMonitoringSummaryDto.builder()
                .totalComplaints(150L)
                .pendingReviewCount(25L)
                .underReviewCount(15L)
                .needsInfoCount(5L)
                .assignedCount(20L)
                .inProgressCount(35L)
                .resolvedCount(30L)
                .closedCount(15L)
                .rejectedCount(5L)
                .criticalPendingCount(8L)
                .resolutionRatePercentage(30.0)
                .statusBreakdown(Map.of("SUBMITTED", 25L, "RESOLVED", 30L, "CLOSED", 15L))
                .departmentBreakdown(Map.of("Roads", 50L, "Electricity", 45L))
                .categoryBreakdown(Map.of("ROADS_AND_FOOTPATHS", 50L))
                .priorityBreakdown(Map.of("CRITICAL", 12L, "HIGH", 40L))
                .build();

        when(monitoringService.getMonitoringSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/admin/monitoring/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalComplaints").value(150))
                .andExpect(jsonPath("$.data.pendingReviewCount").value(25))
                .andExpect(jsonPath("$.data.criticalPendingCount").value(8))
                .andExpect(jsonPath("$.data.resolutionRatePercentage").value(30.0))
                .andExpect(jsonPath("$.data.statusBreakdown.SUBMITTED").value(25))
                .andExpect(jsonPath("$.data.departmentBreakdown.Roads").value(50));
    }
}
