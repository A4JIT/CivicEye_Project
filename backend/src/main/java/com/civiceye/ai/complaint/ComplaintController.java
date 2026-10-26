package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.civiceye.ai.ai.AiRecommendationService;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/complaints", "/api/complaints"})
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintLifecycleService lifecycleService;
    private final AdminComplaintService adminComplaintService;
    private final AiRecommendationService aiRecommendationService;
    private final FeedbackService feedbackService;
    private final ComplaintHistoryService historyService;
    private final DuplicateDetectionService duplicateDetectionService;

    public ComplaintController(ComplaintService complaintService) {
        this(complaintService, null, null, null, null, null, null);
    }

    public ComplaintController(ComplaintService complaintService,
                               ComplaintLifecycleService lifecycleService,
                               AdminComplaintService adminComplaintService) {
        this(complaintService, lifecycleService, adminComplaintService, null, null, null, null);
    }

    public ComplaintController(ComplaintService complaintService,
                               ComplaintLifecycleService lifecycleService,
                               AdminComplaintService adminComplaintService,
                               AiRecommendationService aiRecommendationService) {
        this(complaintService, lifecycleService, adminComplaintService, aiRecommendationService, null, null, null);
    }

    public ComplaintController(ComplaintService complaintService,
                               ComplaintLifecycleService lifecycleService,
                               AdminComplaintService adminComplaintService,
                               AiRecommendationService aiRecommendationService,
                               FeedbackService feedbackService) {
        this(complaintService, lifecycleService, adminComplaintService, aiRecommendationService, feedbackService, null, null);
    }

    public ComplaintController(ComplaintService complaintService,
                               ComplaintLifecycleService lifecycleService,
                               AdminComplaintService adminComplaintService,
                               AiRecommendationService aiRecommendationService,
                               FeedbackService feedbackService,
                               ComplaintHistoryService historyService) {
        this(complaintService, lifecycleService, adminComplaintService, aiRecommendationService, feedbackService, historyService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ComplaintController(ComplaintService complaintService,
                               ComplaintLifecycleService lifecycleService,
                               AdminComplaintService adminComplaintService,
                               AiRecommendationService aiRecommendationService,
                               FeedbackService feedbackService,
                               ComplaintHistoryService historyService,
                               DuplicateDetectionService duplicateDetectionService) {
        this.complaintService = complaintService;
        this.lifecycleService = lifecycleService;
        this.adminComplaintService = adminComplaintService;
        this.aiRecommendationService = aiRecommendationService;
        this.feedbackService = feedbackService;
        this.historyService = historyService;
        this.duplicateDetectionService = duplicateDetectionService;
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

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatusJson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody StatusUpdateRequest request) {
        ComplaintResponse response = lifecycleService.updateStatus(id, request, null, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatusMultipart(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @ModelAttribute @Valid StatusUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        ComplaintResponse response = lifecycleService.updateStatus(id, request, image, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AssignComplaintRequest request) {
        ComplaintResponse response = lifecycleService.assignComplaint(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned successfully", response));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getTimeline(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<TimelineEventResponse> timeline = lifecycleService.getTimeline(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint timeline retrieved successfully", timeline));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getDepartmentComplaints(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ComplaintResponse> complaints = lifecycleService.getComplaintsByDepartment(departmentId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Department complaints retrieved successfully", complaints));
    }

    @PutMapping(value = "/{id}/provide-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> provideInfoMultipart(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @ModelAttribute @Valid ProvideInfoRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        ComplaintResponse response = adminComplaintService.provideInformation(id, request, image, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Additional information provided successfully", response));
    }

    @PutMapping(value = "/{id}/provide-info", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> provideInfoJson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProvideInfoRequest request) {
        ComplaintResponse response = adminComplaintService.provideInformation(id, request, null, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Additional information provided successfully", response));
    }

    @GetMapping("/{id}/recommendation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> getComplaintRecommendation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (aiRecommendationService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        AiRecommendationResponse response = aiRecommendationService.getComplaintRecommendation(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("AI recommendation retrieved successfully", response));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        FeedbackResponse response = feedbackService != null
                ? feedbackService.submitFeedback(id, request, currentUser)
                : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", response));
    }

    @GetMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('CITIZEN', 'DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FeedbackResponse response = feedbackService != null
                ? feedbackService.getFeedbackByComplaintId(id, currentUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success("Feedback retrieved successfully", response));
    }

    @PostMapping("/{id}/confirm-resolution")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> confirmResolutionPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) ConfirmResolutionRequest request) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.confirmResolution(id, request != null ? request : new ConfirmResolutionRequest(), currentUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success("Complaint resolution confirmed and complaint closed", response));
    }

    @PutMapping("/{id}/confirm-resolution")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> confirmResolutionPut(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) ConfirmResolutionRequest request) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.confirmResolution(id, request != null ? request : new ConfirmResolutionRequest(), currentUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success("Complaint resolution confirmed and complaint closed", response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> closeComplaintPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) CloseComplaintRequest request) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.closeComplaint(id, request != null ? request : new CloseComplaintRequest(), currentUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success("Complaint closed successfully", response));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> closeComplaintPut(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) CloseComplaintRequest request) {
        ComplaintResponse response = lifecycleService != null
                ? lifecycleService.closeComplaint(id, request != null ? request : new CloseComplaintRequest(), currentUser)
                : null;
        return ResponseEntity.ok(ApiResponse.success("Complaint closed successfully", response));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ComplaintTimelineResponse>> getComplaintHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (historyService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        ComplaintTimelineResponse response = historyService.getComplaintHistory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint history and timeline retrieved successfully", response));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ComplaintAssignmentResponse>>> getComplaintAssignments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (historyService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        List<ComplaintAssignmentResponse> response = historyService.getAssignmentHistory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Complaint assignment history retrieved successfully", response));
    }

    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkDuplicateComplaint(
            @RequestBody DuplicateCheckRequest request) {
        DuplicateCheckResponse response = duplicateDetectionService != null
                ? duplicateDetectionService.checkDuplicates(request)
                : DuplicateCheckResponse.builder().isDuplicate(false).highestScore(0.0).matches(java.util.Collections.emptyList()).build();
        return ResponseEntity.ok(ApiResponse.success("Duplicate check completed", response));
    }

    @PostMapping("/{id}/upvote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> upvoteComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (duplicateDetectionService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        java.util.Map<String, Object> response = duplicateDetectionService.upvoteComplaint(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Upvote processed successfully", response));
    }

    @PostMapping("/{id}/merge-duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> mergeDuplicate(
            @PathVariable Long id,
            @RequestParam String parentId,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (duplicateDetectionService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        java.util.Map<String, Object> response = duplicateDetectionService.mergeDuplicate(id, parentId, currentUser, remarks);
        return ResponseEntity.ok(ApiResponse.success("Duplicate merged successfully", response));
    }
}
