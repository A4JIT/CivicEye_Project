package com.civiceye.ai.ai;

import com.civiceye.ai.ai.dto.AiRecommendRequest;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiRecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiRecommendationService aiRecommendationService;

    @InjectMocks
    private AiRecommendationController aiRecommendationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiRecommendationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/ai/recommend should return 200 with calculated recommendation")
    void recommend_V1Endpoint_Success() throws Exception {
        AiRecommendRequest request = new AiRecommendRequest("Massive water pipeline burst", "Flooding street", "MG Road");

        AiRecommendationResponse mockResponse = AiRecommendationResponse.builder()
                .suggestedCategory("WATER_SUPPLY")
                .suggestedPriority("CRITICAL")
                .suggestedDepartmentName("Water Supply & Sewerage Board")
                .suggestedDepartmentCode("WTR")
                .confidenceScore(0.85)
                .explanation("Category identified via keywords; priority calculated via keywords")
                .build();

        when(aiRecommendationService.calculateRecommendation(any(AiRecommendRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("AI recommendation generated successfully"))
                .andExpect(jsonPath("$.data.suggestedCategory").value("WATER_SUPPLY"))
                .andExpect(jsonPath("$.data.suggestedPriority").value("CRITICAL"))
                .andExpect(jsonPath("$.data.suggestedDepartmentCode").value("WTR"))
                .andExpect(jsonPath("$.data.confidenceScore").value(0.85));
    }

    @Test
    @DisplayName("POST /api/ai/recommend (legacy path) should also return 200 with recommendation")
    void recommend_LegacyEndpoint_Success() throws Exception {
        AiRecommendRequest request = new AiRecommendRequest("Pothole on street", "Dangerous for bikes", "Downtown");

        AiRecommendationResponse mockResponse = AiRecommendationResponse.builder()
                .suggestedCategory("ROADS_AND_FOOTPATHS")
                .suggestedPriority("HIGH")
                .confidenceScore(0.75)
                .build();

        when(aiRecommendationService.calculateRecommendation(any(AiRecommendRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.suggestedCategory").value("ROADS_AND_FOOTPATHS"))
                .andExpect(jsonPath("$.data.suggestedPriority").value("HIGH"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/recommend with blank title should return 400 Bad Request")
    void recommend_BlankTitle_ReturnsBadRequest() throws Exception {
        AiRecommendRequest request = new AiRecommendRequest("", "Some description", "Location");

        mockMvc.perform(post("/api/v1/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
