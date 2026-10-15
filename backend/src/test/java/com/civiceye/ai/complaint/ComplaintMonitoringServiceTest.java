package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintFilterRequest;
import com.civiceye.ai.complaint.dto.ComplaintMonitoringSummaryDto;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintMonitoringServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ComplaintMonitoringService monitoringService;

    private Department roadDept;

    @BeforeEach
    void setUp() {
        roadDept = Department.builder().id(1L).name("Road Works").code("ROAD").build();
    }

    @Test
    @DisplayName("Should aggregate complete monitoring summary KPI counts and resolution rate")
    void getMonitoringSummaryCalculatesCorrectMetrics() {
        when(complaintRepository.count()).thenReturn(100L);
        when(complaintRepository.countByStatus(ComplaintStatus.SUBMITTED)).thenReturn(20L);
        when(complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW)).thenReturn(10L);
        when(complaintRepository.countByStatus(ComplaintStatus.NEEDS_INFORMATION)).thenReturn(5L);
        when(complaintRepository.countByStatus(ComplaintStatus.ASSIGNED)).thenReturn(15L);
        when(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(20L);
        when(complaintRepository.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(18L);
        when(complaintRepository.countByStatus(ComplaintStatus.CLOSED)).thenReturn(10L);
        when(complaintRepository.countByStatus(ComplaintStatus.REJECTED)).thenReturn(2L);

        when(departmentRepository.findAll()).thenReturn(List.of(roadDept));
        when(complaintRepository.countByDepartmentId(1L)).thenReturn(40L);

        ComplaintMonitoringSummaryDto summary = monitoringService.getMonitoringSummary();

        assertNotNull(summary);
        assertEquals(100L, summary.getTotalComplaints());
        assertEquals(20L, summary.getPendingReviewCount());
        assertEquals(18L, summary.getResolvedCount());
        assertEquals(10L, summary.getClosedCount());
        assertEquals(28.0, summary.getResolutionRatePercentage()); // (18 + 10) / 100 * 100 = 28.0%
        assertEquals(40L, summary.getDepartmentBreakdown().get("Road Works"));
    }

    @Test
    @DisplayName("Should filter complaints with specification and map to ComplaintResponse page")
    void filterComplaintsReturnsPageOfResponses() {
        Complaint c = Complaint.builder()
                .id(50L)
                .complaintNumber("CE-TEST-50")
                .title("Streetlamp out")
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<Complaint> page = new PageImpl<>(List.of(c));
        when(complaintRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ComplaintFilterRequest req = ComplaintFilterRequest.builder()
                .status(ComplaintStatus.SUBMITTED)
                .page(0)
                .size(10)
                .build();

        Page<ComplaintResponse> result = monitoringService.filterComplaints(req);

        assertEquals(1, result.getTotalElements());
        assertEquals("CE-TEST-50", result.getContent().get(0).getComplaintNumber());
    }
}
