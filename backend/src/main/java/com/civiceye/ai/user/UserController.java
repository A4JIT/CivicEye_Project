package com.civiceye.ai.user;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.user.dto.CreateDepartmentStaffRequest;
import com.civiceye.ai.user.dto.DepartmentStaffResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/department-staff", "/api/admin/department-staff"})
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentStaffResponse>> createDepartmentStaff(
            @Valid @RequestBody CreateDepartmentStaffRequest request) {
        DepartmentStaffResponse response = userService.createDepartmentStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department staff account created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentStaffResponse>>> getAllDepartmentStaff() {
        List<DepartmentStaffResponse> list = userService.getAllDepartmentStaff();
        return ResponseEntity.ok(ApiResponse.success("Department staff list retrieved successfully", list));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<DepartmentStaffResponse>> toggleStaffStatus(@PathVariable Long id) {
        DepartmentStaffResponse response = userService.toggleDepartmentStaffStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Staff status updated successfully", response));
    }
}
