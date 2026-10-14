package com.civiceye.ai.complaint;

import com.civiceye.ai.ai.AiRecommendationService;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.CreateComplaintRequest;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComplaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplaintService complaintService;

    @Mock
    private ComplaintLifecycleService lifecycleService;

    @Mock
    private AdminComplaintService adminComplaintService;

    @Mock
    private AiRecommendationService aiRecommendationService;

    @InjectMocks
    private ComplaintController complaintController;

    private ObjectMapper objectMapper;
    private ComplaintResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(complaintController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleResponse = ComplaintResponse.builder()
                .id(1L)
                .complaintNumber("CE-2026-0001")
                .title("Damaged street light")
                .description("Light pole fixture broken near park")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .location("Central Park Gate 2")
                .citizenId(1L)
                .citizenName("Citizen John")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/complaints should return 201 Created with complaint details on JSON submission")
    void createComplaintJson_Returns201_OnSuccess() throws Exception {
        CreateComplaintRequest request = CreateComplaintRequest.builder()
                .title("Damaged street light")
                .description("Light pole fixture broken near park")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .location("Central Park Gate 2")
                .build();

        when(complaintService.createComplaint(any(CreateComplaintRequest.class), eq(null), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Complaint registered successfully"))
                .andExpect(jsonPath("$.data.complaintNumber").value("CE-2026-0001"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("POST /api/complaints with multipart file should return 201 Created")
    void createComplaintMultipart_Returns201_OnSuccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("image", "pole.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(complaintService.createComplaint(any(CreateComplaintRequest.class), any(), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(multipart("/api/complaints")
                        .file(imageFile)
                        .param("title", "Damaged street light")
                        .param("description", "Light pole fixture broken near park")
                        .param("category", "STREETLIGHTS_AND_ELECTRICITY")
                        .param("location", "Central Park Gate 2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CE-2026-0001"));
    }

    @Test
    @DisplayName("POST /api/complaints should return 400 Bad Request when mandatory fields are missing")
    void createComplaint_MissingTitle_Returns400() throws Exception {
        CreateComplaintRequest invalidRequest = CreateComplaintRequest.builder()
                .title("") // Blank title
                .description("Some description")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .location("Some location")
                .build();

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/complaints/my should return 200 with citizen complaint list")
    void getMyComplaints_Returns200() throws Exception {
        when(complaintService.getMyComplaints(any())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].complaintNumber").value("CE-2026-0001"));
    }

    @Test
    @DisplayName("GET /api/complaints/{id} should return 200 with complaint details")
    void getComplaintById_Returns200() throws Exception {
        when(complaintService.getComplaintById(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/complaints/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Damaged street light"));
    }

    @Test
    @DisplayName("GET /api/complaints/number/{number} should return 200 with complaint details")
    void getComplaintByNumber_Returns200() throws Exception {
        when(complaintService.getComplaintByNumber(eq("CE-2026-0001"), any())).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/complaints/number/CE-2026-0001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CE-2026-0001"));
    }

    @Test
    @DisplayName("PUT /api/complaints/{id}/status should return 200 with updated status")
    void updateStatus_Returns200() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.IN_PROGRESS);
        when(lifecycleService.updateStatus(eq(1L), any(), any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/complaints/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\", \"remarks\": \"Inspection started\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PUT /api/complaints/{id}/assign should return 200 with assignment confirmation")
    void assignComplaint_Returns200() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.ASSIGNED);
        sampleResponse.setDepartmentId(10L);
        when(lifecycleService.assignComplaint(eq(1L), any(), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/complaints/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\": 10, \"instructions\": \"Immediate inspection needed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("GET /api/complaints/{id}/timeline should return 200 with audit timeline")
    void getTimeline_Returns200() throws Exception {
        when(lifecycleService.getTimeline(eq(1L), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/complaints/1/timeline")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/complaints/department/{deptId} should return 200 with department complaints")
    void getDepartmentComplaints_Returns200() throws Exception {
        when(lifecycleService.getComplaintsByDepartment(eq(10L), any())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints/department/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/complaints/{id}/recommendation should return 200 with AI recommendation")
    void getComplaintRecommendation_Returns200() throws Exception {
        AiRecommendationResponse recResponse = AiRecommendationResponse.builder()
                .id(100L)
                .complaintId(1L)
                .suggestedCategory("STREETLIGHTS_AND_ELECTRICITY")
                .suggestedPriority("MEDIUM")
                .suggestedDepartmentName("Electricity & Street Lighting")
                .confidenceScore(0.80)
                .build();

        when(aiRecommendationService.getComplaintRecommendation(eq(1L), any())).thenReturn(recResponse);

        mockMvc.perform(get("/api/v1/complaints/1/recommendation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.complaintId").value(1))
                .andExpect(jsonPath("$.data.suggestedCategory").value("STREETLIGHTS_AND_ELECTRICITY"));
    }
}
