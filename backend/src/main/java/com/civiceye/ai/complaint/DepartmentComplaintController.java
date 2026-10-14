package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department/complaints")
@PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
public class DepartmentComplaintController {

    private final DepartmentComplaintService departmentComplaintService;

    public DepartmentComplaintController(DepartmentComplaintService departmentComplaintService) {
        this.departmentComplaintService = departmentComplaintService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentComplaints(
            @RequestParam(required = false) List<ComplaintStatus> status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @AuthenticationPrincipal UserPrincipal currentUser) {

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
        Page<ComplaintResponse> resultPage = departmentComplaintService.searchDepartmentComplaints(
                status, priority, departmentId, search, pageable, currentUser);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("complaints", resultPage.getContent());
        responseData.put("currentPage", resultPage.getNumber());
        responseData.put("totalItems", resultPage.getTotalElements());
        responseData.put("totalPages", resultPage.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success(
                "Department complaints retrieved successfully", responseData));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentComplaintDetailResponse>> getDepartmentComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        DepartmentComplaintDetailResponse response = departmentComplaintService.getDepartmentComplaintById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Department complaint details retrieved successfully", response));
    }

    @PutMapping("/{id}/start-work")
    public ResponseEntity<ApiResponse<ComplaintResponse>> startWork(
            @PathVariable Long id,
            @RequestBody(required = false) StartWorkRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ComplaintResponse response = departmentComplaintService.startWork(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Work started on complaint successfully", response));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<ComplaintResponse>> addProgressNote(
            @PathVariable Long id,
            @Valid @RequestBody ProgressNoteRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ComplaintResponse response = departmentComplaintService.addProgressNote(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Progress note recorded successfully", response));
    }

    @PostMapping(value = "/{id}/resolve", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaintJson(
            @PathVariable Long id,
            @Valid @RequestBody ResolveComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ComplaintResponse response = departmentComplaintService.resolveComplaint(id, request, null, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint resolved successfully", response));
    }

    @PostMapping(value = "/{id}/resolve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaintMultipart(
            @PathVariable Long id,
            @ModelAttribute @Valid ResolveComplaintRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ComplaintResponse response = departmentComplaintService.resolveComplaint(id, request, image, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint resolved successfully", response));
    }
}
