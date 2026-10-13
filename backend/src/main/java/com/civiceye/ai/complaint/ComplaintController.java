package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.CreateComplaintRequest;
import com.civiceye.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaintMultipart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @ModelAttribute @Valid CreateComplaintRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Long citizenId = currentUser != null ? currentUser.getId() : 1L;
        ComplaintResponse response = complaintService.createComplaint(request, image, citizenId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint registered successfully", response));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaintJson(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateComplaintRequest request) {
        Long citizenId = currentUser != null ? currentUser.getId() : 1L;
        ComplaintResponse response = complaintService.createComplaint(request, null, citizenId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint registered successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long citizenId = currentUser != null ? currentUser.getId() : 1L;
        List<ComplaintResponse> complaints = complaintService.getMyComplaints(citizenId);
        return ResponseEntity.ok(ApiResponse.success("User complaints retrieved successfully", complaints));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN', 'DEPARTMENT_USER')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponse response = complaintService.getComplaintById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", response));
    }

    @GetMapping("/number/{complaintNumber}")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN', 'DEPARTMENT_USER')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintByNumber(
            @PathVariable String complaintNumber,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ComplaintResponse response = complaintService.getComplaintByNumber(complaintNumber, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints() {
        List<ComplaintResponse> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.success("All complaints retrieved", complaints));
    }
}
