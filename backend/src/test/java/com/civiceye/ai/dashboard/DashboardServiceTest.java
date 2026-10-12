package com.civiceye.ai.dashboard;

import com.civiceye.ai.complaint.*;
import com.civiceye.ai.dashboard.dto.AdminDashboardResponse;
import com.civiceye.ai.dashboard.dto.CitizenDashboardResponse;
import com.civiceye.ai.dashboard.dto.DepartmentDashboardResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User sampleCitizen;
    private Department sampleDepartment;
    private Complaint sampleComplaint;

    @BeforeEach
    void setUp() {
        sampleCitizen = User.builder()
                .id(1L)
                .username("citizen_jane")
                .email("citizen@civiceye.com")
                .fullName("Jane Citizen")
                .role(Role.CITIZEN)
                .build();

        sampleDepartment = Department.builder()
                .id(1L)
                .name("Public Works & Roads")
                .code("ROAD_WORKS")
                .description("Roads and infrastructure")
                .isActive(true)
                .build();

        sampleComplaint = Complaint.builder()
                .id(101L)
                .complaintNumber("CE-2026-0001")
                .title("Pothole on Main Street")
                .description("Hazardous pothole")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.HIGH)
                .location("Main Street 12")
                .citizen(sampleCitizen)
                .department(sampleDepartment)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should calculate citizen dashboard metrics accurately")
    void getCitizenDashboard_Success() {
        when(complaintRepository.countByCitizenId(1L)).thenReturn(5L);
        when(complaintRepository.countByCitizenIdAndStatus(1L, ComplaintStatus.SUBMITTED)).thenReturn(2L);
        when(complaintRepository.countByCitizenIdAndStatus(1L, ComplaintStatus.UNDER_REVIEW)).thenReturn(1L);
        when(complaintRepository.countByCitizenIdAndStatus(1L, ComplaintStatus.IN_PROGRESS)).thenReturn(1L);
        when(complaintRepository.countByCitizenIdAndStatus(1L, ComplaintStatus.RESOLVED)).thenReturn(1L);
        when(complaintRepository.countByCitizenIdAndStatus(1L, ComplaintStatus.REJECTED)).thenReturn(0L);
        when(complaintRepository.findTop5ByCitizenIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(sampleComplaint));

        CitizenDashboardResponse response = dashboardService.getCitizenDashboard(1L);

        assertNotNull(response);
        assertEquals(5L, response.getTotalComplaints());
        assertEquals(2L, response.getSubmittedCount());
        assertEquals(1L, response.getUnderReviewCount());
        assertEquals(1L, response.getInProgressCount());
        assertEquals(1L, response.getResolvedCount());
        assertEquals(0L, response.getRejectedCount());
        assertEquals(1, response.getRecentComplaints().size());
        assertEquals("CE-2026-0001", response.getRecentComplaints().get(0).getComplaintNumber());
    }

    @Test
    @DisplayName("Should handle citizen dashboard when user has zero complaints")
    void getCitizenDashboard_ZeroComplaints() {
        when(complaintRepository.countByCitizenId(2L)).thenReturn(0L);
        when(complaintRepository.countByCitizenIdAndStatus(eq(2L), any())).thenReturn(0L);
        when(complaintRepository.findTop5ByCitizenIdOrderByCreatedAtDesc(2L)).thenReturn(Collections.emptyList());

        CitizenDashboardResponse response = dashboardService.getCitizenDashboard(2L);

        assertNotNull(response);
        assertEquals(0L, response.getTotalComplaints());
        assertEquals(0L, response.getSubmittedCount());
        assertTrue(response.getRecentComplaints().isEmpty());
    }

    @Test
    @DisplayName("Should aggregate administrator metrics and category breakdown")
    void getAdminDashboard_Success() {
        when(complaintRepository.count()).thenReturn(10L);
        when(complaintRepository.countByStatus(ComplaintStatus.SUBMITTED)).thenReturn(3L);
        when(complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(3L);
        when(complaintRepository.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.REJECTED)).thenReturn(0L);

        when(departmentRepository.findAll()).thenReturn(List.of(sampleDepartment));
        when(complaintRepository.countByDepartmentId(1L)).thenReturn(4L);
        when(complaintRepository.countByCategory(ComplaintCategory.ROADS_AND_FOOTPATHS)).thenReturn(5L);
        when(complaintRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(sampleComplaint));

        AdminDashboardResponse response = dashboardService.getAdminDashboard();

        assertNotNull(response);
        assertEquals(10L, response.getTotalComplaints());
        assertEquals(5L, response.getPendingReviewCount());
        assertEquals(2L, response.getResolvedCount());
        assertEquals(20.0, response.getResolutionRatePercentage());
        assertTrue(response.getComplaintsByDepartment().containsKey("Public Works & Roads"));
        assertEquals(1, response.getRecentComplaints().size());
    }

    @Test
    @DisplayName("Should calculate department dashboard metrics and critical alerts")
    void getDepartmentDashboard_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(complaintRepository.countByDepartmentId(1L)).thenReturn(8L);
        when(complaintRepository.countByDepartmentIdAndStatus(1L, ComplaintStatus.ASSIGNED)).thenReturn(3L);
        when(complaintRepository.countByDepartmentIdAndStatus(1L, ComplaintStatus.IN_PROGRESS)).thenReturn(3L);
        when(complaintRepository.countByDepartmentIdAndStatus(1L, ComplaintStatus.RESOLVED)).thenReturn(2L);
        when(complaintRepository.countCriticalPendingByDepartment(1L)).thenReturn(2L);
        when(complaintRepository.findTop5ByDepartmentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(sampleComplaint));

        DepartmentDashboardResponse response = dashboardService.getDepartmentDashboard(1L);

        assertNotNull(response);
        assertEquals(1L, response.getDepartmentId());
        assertEquals("Public Works & Roads", response.getDepartmentName());
        assertEquals("ROAD_WORKS", response.getDepartmentCode());
        assertEquals(8L, response.getTotalAssignedComplaints());
        assertEquals(3L, response.getPendingActionCount());
        assertEquals(2L, response.getCriticalAlertsCount());
        assertEquals(1, response.getRecentAssignedComplaints().size());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department does not exist")
    void getDepartmentDashboard_NotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            dashboardService.getDepartmentDashboard(99L);
        });
    }
}
