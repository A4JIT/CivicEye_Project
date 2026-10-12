package com.civiceye.ai.dashboard;

import com.civiceye.ai.dashboard.dto.AdminDashboardResponse;
import com.civiceye.ai.dashboard.dto.CitizenDashboardResponse;
import com.civiceye.ai.dashboard.dto.DepartmentDashboardResponse;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.civiceye.ai.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/dashboard/citizen should return 200 OK with citizen dashboard metrics")
    void getCitizenDashboard_Returns200_WithCitizenData() throws Exception {
        CitizenDashboardResponse response = CitizenDashboardResponse.builder()
                .citizenId(1L)
                .citizenName("John Doe")
                .totalComplaints(5L)
                .pendingComplaints(2L)
                .inProgressComplaints(1L)
                .resolvedComplaints(2L)
                .rejectedComplaints(0L)
                .recentComplaints(Collections.emptyList())
                .build();

        when(dashboardService.getCitizenDashboard(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/citizen")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Citizen dashboard metrics retrieved"))
                .andExpect(jsonPath("$.data.citizenId").value(1))
                .andExpect(jsonPath("$.data.citizenName").value("John Doe"))
                .andExpect(jsonPath("$.data.totalComplaints").value(5))
                .andExpect(jsonPath("$.data.resolvedComplaints").value(2));
    }

    @Test
    @DisplayName("GET /api/dashboard/admin should return 200 OK with admin overview metrics")
    void getAdminDashboard_Returns200_WithAdminData() throws Exception {
        Map<String, Long> complaintsByDept = new HashMap<>();
        complaintsByDept.put("Roads & Traffic", 5L);

        Map<String, Long> complaintsByCat = new HashMap<>();
        complaintsByCat.put("POTHOLE", 4L);

        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .totalComplaints(15L)
                .pendingComplaints(5L)
                .inProgressComplaints(6L)
                .resolvedComplaints(3L)
                .rejectedComplaints(1L)
                .totalCitizens(10L)
                .totalDepartments(4L)
                .criticalComplaints(2L)
                .highComplaints(5L)
                .complaintsByDepartment(complaintsByDept)
                .complaintsByCategory(complaintsByCat)
                .recentComplaints(Collections.emptyList())
                .build();

        when(dashboardService.getAdminDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Administrator dashboard metrics retrieved"))
                .andExpect(jsonPath("$.data.totalComplaints").value(15))
                .andExpect(jsonPath("$.data.totalCitizens").value(10))
                .andExpect(jsonPath("$.data.criticalComplaints").value(2));
    }

    @Test
    @DisplayName("GET /api/dashboard/department should return 200 OK with department metrics")
    void getDepartmentDashboard_Returns200_WithDepartmentData() throws Exception {
        DepartmentDashboardResponse response = DepartmentDashboardResponse.builder()
                .departmentId(1L)
                .departmentName("Roads & Traffic")
                .departmentCode("ROADS")
                .totalAssigned(8L)
                .pendingComplaints(3L)
                .inProgressComplaints(3L)
                .resolvedComplaints(2L)
                .criticalCount(1L)
                .highCount(2L)
                .mediumCount(3L)
                .lowCount(2L)
                .recentComplaints(Collections.emptyList())
                .build();

        when(dashboardService.getDepartmentDashboard(1L)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/department")
                        .param("departmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department dashboard metrics retrieved"))
                .andExpect(jsonPath("$.data.departmentId").value(1))
                .andExpect(jsonPath("$.data.departmentName").value("Roads & Traffic"))
                .andExpect(jsonPath("$.data.totalAssigned").value(8));
    }

    @Test
    @DisplayName("GET /api/dashboard/department should return 404 when department not found")
    void getDepartmentDashboard_Returns404_WhenDepartmentNotFound() throws Exception {
        when(dashboardService.getDepartmentDashboard(99L))
                .thenThrow(new ResourceNotFoundException("Department", "id", 99L));

        mockMvc.perform(get("/api/dashboard/department")
                        .param("departmentId", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Department not found with id : '99'"));
    }
}
