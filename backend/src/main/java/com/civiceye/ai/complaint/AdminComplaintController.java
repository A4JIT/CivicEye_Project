package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.civiceye.ai.ai.AiRecommendationService;
import com.civiceye.ai.ai.dto.AiOverrideRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/complaints", "/api/admin/complaints"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;
    private final AiRecommendationService aiRecommendationService;
    private final ComplaintLifecycleService lifecycleService;

    public AdminComplaintController(AdminComplaintService adminComplaintService) {
        this(adminComplaintService, null, null);
    }

    public AdminComplaintController(AdminComplaintService adminComplaintService,
                                    AiRecommendationService aiRecommendationService) {
        this(adminComplaintService, aiRecommendationService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AdminComplaintController(AdminComplaintService adminComplaintService,
                                    AiRecommendationService aiRecommendationService,
                                    ComplaintLifecycleService lifecycleService) {
        this.adminComplaintService = adminComplaintService;
        this.aiRecommendationService = aiRecommendationService;
        this.lifecycleService = lifecycleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchComplaints(
            @RequestParam(required = false) List<ComplaintStatus> status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sortObj = Sort.by(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<ComplaintResponse> resultPage = adminComplaintService.searchComplaints(
                status, departmentId, priority, category, search, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("complaints", resultPage.getContent());
        responseData.put("currentPage", resultPage.getNumber());
        responseData.put("totalItems", resultPage.getTotalElements());
        responseData.put("totalPages", resultPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(
                "Complaints retrieved successfully", responseData));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ComplaintResponse>> reviewComplaint(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {

        ComplaintResponse response = adminComplaintService.reviewComplaint(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint moved to review status successfully", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ComplaintResponse>> rejectComplaint(
            @PathVariable Long id,
            @Valid @RequestBody RejectComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {

        ComplaintResponse response = adminComplaintService.rejectComplaint(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint rejected with reason recorded", response));
    }

    @PutMapping("/{id}/request-info")
    public ResponseEntity<ApiResponse<ComplaintResponse>> requestInformation(
            @PathVariable Long id,
            @Valid @RequestBody RequestInfoRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {

        ComplaintResponse response = adminComplaintService.requestInformation(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Information request submitted to citizen successfully", response));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignDepartment(
            @PathVariable Long id,
            @Valid @RequestBody AdminAssignRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {

        ComplaintResponse response = adminComplaintService.assignDepartment(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint successfully assigned to department", response));
    }

    @PostMapping("/{id}/apply-recommendation")
    public ResponseEntity<ApiResponse<ComplaintResponse>> applyRecommendation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        ComplaintResponse response = aiRecommendationService.applyRecommendation(id, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "AI recommendation applied successfully in 1-click triage", response));
    }

    @PostMapping("/{id}/override-recommendation")
    public ResponseEntity<ApiResponse<ComplaintResponse>> overrideRecommendation(
            @PathVariable Long id,
            @RequestBody(required = false) AiOverrideRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        ComplaintResponse response = aiRecommendationService.overrideRecommendation(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success(
                "AI recommendation overridden successfully by administrator", response));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ComplaintResponse>> closeComplaintPost(
            @PathVariable Long id,
            @RequestBody(required = false) CloseComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.closeComplaint(id, request != null ? request : new CloseComplaintRequest(), adminUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint closed successfully by administrator", response));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ComplaintResponse>> closeComplaintPut(
            @PathVariable Long id,
            @RequestBody(required = false) CloseComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal adminUser) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.closeComplaint(id, request != null ? request : new CloseComplaintRequest(), adminUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint closed successfully by administrator", response));
    }
}
