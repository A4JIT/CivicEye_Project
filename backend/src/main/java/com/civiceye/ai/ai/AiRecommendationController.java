package com.civiceye.ai.ai;

import com.civiceye.ai.ai.dto.AiRecommendRequest;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;
import com.civiceye.ai.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/ai", "/api/ai"})
@PreAuthorize("isAuthenticated()")
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    public AiRecommendationController(AiRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> getRealtimeRecommendation(
            @Valid @RequestBody AiRecommendRequest request) {
        AiRecommendationResponse response = aiRecommendationService.calculateRecommendation(request);
        return ResponseEntity.ok(ApiResponse.success("AI recommendation generated successfully", response));
    }
}
