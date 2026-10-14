package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.DepartmentFeedbackStats;
import com.civiceye.ai.complaint.dto.FeedbackResponse;
import com.civiceye.ai.complaint.dto.FeedbackStatsResponse;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private FeedbackController feedbackController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedbackController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/feedback/stats returns 200 with aggregated satisfaction statistics")
    void getFeedbackStats_Returns200() throws Exception {
        FeedbackStatsResponse stats = FeedbackStatsResponse.builder()
                .overallAverageRating(4.65)
                .totalReviews(24L)
                .ratingDistribution(Map.of(1, 1L, 2, 0L, 3, 2L, 4, 6L, 5, 15L))
                .departmentStats(List.of(
                        DepartmentFeedbackStats.builder()
                                .departmentId(1L)
                                .departmentName("Roads & Infrastructure")
                                .departmentCode("ROAD_WORKS")
                                .averageRating(4.5)
                                .totalReviews(14L)
                                .build()
                ))
                .build();

        when(feedbackService.getFeedbackStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/feedback/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallAverageRating").value(4.65))
                .andExpect(jsonPath("$.data.totalReviews").value(24))
                .andExpect(jsonPath("$.data.ratingDistribution['5']").value(15))
                .andExpect(jsonPath("$.data.departmentStats[0].departmentCode").value("ROAD_WORKS"));
    }

    @Test
    @DisplayName("GET /api/v1/feedback/department/{id} returns 200 with department feedback entries")
    void getDepartmentFeedback_Returns200() throws Exception {
        FeedbackResponse feedback = FeedbackResponse.builder()
                .id(1L)
                .complaintId(101L)
                .complaintNumber("CE-2026-1001")
                .citizenId(5L)
                .citizenName("Jane Doe")
                .departmentId(1L)
                .departmentName("Roads & Infrastructure")
                .rating(5)
                .comments("Very rapid road repair.")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackService.getFeedbackByDepartment(eq(1L), any())).thenReturn(List.of(feedback));

        mockMvc.perform(get("/api/v1/feedback/department/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].rating").value(5))
                .andExpect(jsonPath("$.data[0].comments").value("Very rapid road repair."));
    }
}
