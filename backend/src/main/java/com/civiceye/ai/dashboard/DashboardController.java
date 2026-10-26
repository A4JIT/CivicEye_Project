package com.civiceye.ai.dashboard;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.dashboard.dto.AdminDashboardResponse;
import com.civiceye.ai.dashboard.dto.CitizenDashboardResponse;
import com.civiceye.ai.dashboard.dto.DepartmentDashboardResponse;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/citizen")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<CitizenDashboardResponse>> getCitizenDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long citizenId) {
        Long targetCitizenId;
        if (userPrincipal != null) {
            targetCitizenId = (citizenId != null && userPrincipal.getRole().name().equals("ADMIN"))
                    ? citizenId
                    : userPrincipal.getId();
        } else {
            targetCitizenId = citizenId != null ? citizenId : 1L;
        }

        CitizenDashboardResponse response = dashboardService.getCitizenDashboard(targetCitizenId);
        return ResponseEntity.ok(ApiResponse.success("Citizen dashboard metrics retrieved", response));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Administrator dashboard metrics retrieved", response));
    }

    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDashboardResponse>> getDepartmentDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Long departmentId) {
        Long targetDeptId = departmentId;
        if (targetDeptId == null && userPrincipal != null && userPrincipal.getRole() == Role.DEPARTMENT_USER) {
            User officer = userRepository.findById(userPrincipal.getId()).orElse(null);
            if (officer != null && officer.getDepartment() != null) {
                targetDeptId = officer.getDepartment().getId();
            }
        }
        if (targetDeptId == null) {
            targetDeptId = 1L;
        }
        DepartmentDashboardResponse response = dashboardService.getDepartmentDashboard(targetDeptId);
        return ResponseEntity.ok(ApiResponse.success("Department dashboard metrics retrieved", response));
    }
}
