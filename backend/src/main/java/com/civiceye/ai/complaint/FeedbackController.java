package com.civiceye.ai.complaint;

import com.civiceye.ai.common.ApiResponse;
import com.civiceye.ai.complaint.dto.FeedbackResponse;
import com.civiceye.ai.complaint.dto.FeedbackStatsResponse;
import com.civiceye.ai.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/feedback", "/api/feedback"})
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<FeedbackStatsResponse>> getFeedbackStats() {
        FeedbackStatsResponse stats = feedbackService.getFeedbackStats();
        return ResponseEntity.ok(ApiResponse.success("Satisfaction statistics retrieved successfully", stats));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('DEPARTMENT_USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getDepartmentFeedback(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<FeedbackResponse> responses = feedbackService.getFeedbackByDepartment(departmentId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Department feedback records retrieved successfully", responses));
    }
}
