package com.civiceye.ai.dashboard;

import com.civiceye.ai.complaint.*;
import com.civiceye.ai.dashboard.dto.*;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;

    public DashboardService(ComplaintRepository complaintRepository, DepartmentRepository departmentRepository) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
    }

    public CitizenDashboardResponse getCitizenDashboard(Long citizenId) {
        long total = complaintRepository.countByCitizenId(citizenId);
        long submitted = complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.UNDER_REVIEW);
        long inProgress = complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByCitizenIdAndStatus(citizenId, ComplaintStatus.REJECTED);

        List<ComplaintSummaryDto> recent = complaintRepository.findTop5ByCitizenIdOrderByCreatedAtDesc(citizenId)
                .stream()
                .map(ComplaintSummaryDto::fromEntity)
                .collect(Collectors.toList());

        return CitizenDashboardResponse.builder()
                .totalComplaints(total)
                .submittedCount(submitted)
                .underReviewCount(underReview)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .recentComplaints(recent)
                .build();
    }

    public AdminDashboardResponse getAdminDashboard() {
        long total = complaintRepository.count();
        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW);
        long pendingReview = submitted + underReview;
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        double resolutionRate = total > 0 ? ((double) resolved / total) * 100.0 : 0.0;
        resolutionRate = Math.round(resolutionRate * 10.0) / 10.0;

        Map<String, Long> byCategory = new HashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            long count = complaintRepository.countByCategory(cat);
            if (count > 0) {
                byCategory.put(cat.name(), count);
            }
        }

        Map<String, Long> byDepartment = new HashMap<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            long count = complaintRepository.countByDepartmentId(dept.getId());
            if (count > 0) {
                byDepartment.put(dept.getName(), count);
            }
        }

        List<ComplaintSummaryDto> recent = complaintRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(ComplaintSummaryDto::fromEntity)
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalComplaints(total)
                .pendingReviewCount(pendingReview)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .resolutionRatePercentage(resolutionRate)
                .complaintsByCategory(byCategory)
                .complaintsByDepartment(byDepartment)
                .recentComplaints(recent)
                .build();
    }

    public DepartmentDashboardResponse getDepartmentDashboard(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        long total = complaintRepository.countByDepartmentId(departmentId);
        long pending = complaintRepository.countByDepartmentIdAndStatus(departmentId, ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByDepartmentIdAndStatus(departmentId, ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByDepartmentIdAndStatus(departmentId, ComplaintStatus.RESOLVED);
        long criticalAlerts = complaintRepository.countCriticalPendingByDepartment(departmentId);

        List<ComplaintSummaryDto> recent = complaintRepository.findTop5ByDepartmentIdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(ComplaintSummaryDto::fromEntity)
                .collect(Collectors.toList());

        return DepartmentDashboardResponse.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .departmentCode(department.getCode())
                .totalAssignedComplaints(total)
                .pendingActionCount(pending)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .criticalAlertsCount(criticalAlerts)
                .recentAssignedComplaints(recent)
                .build();
    }
}
