package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import com.civiceye.ai.ai.AiRecommendationService;
import com.civiceye.ai.ai.dto.AiOverrideRequest;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminComplaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminComplaintService adminComplaintService;

    @Mock
    private AiRecommendationService aiRecommendationService;

    @Mock
    private ComplaintLifecycleService lifecycleService;

    @InjectMocks
    private AdminComplaintController adminComplaintController;

    private ObjectMapper objectMapper;
    private ComplaintResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminComplaintController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleResponse = ComplaintResponse.builder()
                .id(1L)
                .complaintNumber("CE-2026-0001")
                .title("Severe pothole on main avenue")
                .description("Dangerous road cavity causing traffic hazard")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.HIGH)
                .location("MG Road Junction")
                .citizenId(2L)
                .citizenName("Jane Citizen")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/admin/complaints returns paginated complaints")
    void testSearchComplaints_Success() throws Exception {
        Page<ComplaintResponse> page = new PageImpl<>(List.of(sampleResponse));
        when(adminComplaintService.searchComplaints(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/complaints")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "pothole")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaints[0].complaintNumber").value("CE-2026-0001"))
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    @DisplayName("PUT /api/admin/complaints/{id}/review returns updated complaint")
    void testReviewComplaint_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.UNDER_REVIEW);
        sampleResponse.setAdminNotes("Reviewed and confirmed");
        when(adminComplaintService.reviewComplaint(eq(1L), any(), any()))
                .thenReturn(sampleResponse);

        AdminReviewRequest request = new AdminReviewRequest("Reviewed and confirmed", null, null);

        mockMvc.perform(put("/api/admin/complaints/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.data.adminNotes").value("Reviewed and confirmed"));
    }

    @Test
    @DisplayName("PUT /api/admin/complaints/{id}/reject returns rejected complaint")
    void testRejectComplaint_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.REJECTED);
        sampleResponse.setRejectionReason("Not a civic matter");
        when(adminComplaintService.rejectComplaint(eq(1L), any(), any()))
                .thenReturn(sampleResponse);

        RejectComplaintRequest request = new RejectComplaintRequest("Not a civic matter", "INVALID");

        mockMvc.perform(put("/api/admin/complaints/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Not a civic matter"));
    }

    @Test
    @DisplayName("PUT /api/admin/complaints/{id}/reject with blank reason returns 400 Bad Request")
    void testRejectComplaint_BlankReason_ReturnsBadRequest() throws Exception {
        RejectComplaintRequest request = new RejectComplaintRequest("");

        mockMvc.perform(put("/api/admin/complaints/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/admin/complaints/{id}/request-info returns updated complaint")
    void testRequestInfo_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.NEEDS_INFORMATION);
        sampleResponse.setNeedsInfoRemarks("Need exact house number");
        when(adminComplaintService.requestInformation(eq(1L), any(), any()))
                .thenReturn(sampleResponse);

        RequestInfoRequest request = new RequestInfoRequest("Need exact house number");

        mockMvc.perform(put("/api/admin/complaints/1/request-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("NEEDS_INFORMATION"))
                .andExpect(jsonPath("$.data.needsInfoRemarks").value("Need exact house number"));
    }

    @Test
    @DisplayName("PUT /api/admin/complaints/{id}/assign returns assigned complaint")
    void testAssignDepartment_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.ASSIGNED);
        sampleResponse.setDepartmentName("Public Works & Roads");
        when(adminComplaintService.assignDepartment(eq(1L), any(), any()))
                .thenReturn(sampleResponse);

        AdminAssignRequest request = new AdminAssignRequest(10L, 5L, Priority.CRITICAL, "Urgent route dispatch");

        mockMvc.perform(put("/api/admin/complaints/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.departmentName").value("Public Works & Roads"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/complaints/{id}/apply-recommendation returns assigned complaint")
    void testApplyRecommendation_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.ASSIGNED);
        sampleResponse.setDepartmentName("Water Supply & Sewerage Board");
        when(aiRecommendationService.applyRecommendation(eq(1L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/admin/complaints/1/apply-recommendation")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("AI recommendation applied successfully in 1-click triage"))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/complaints/{id}/override-recommendation returns updated complaint")
    void testOverrideRecommendation_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.UNDER_REVIEW);
        sampleResponse.setCategory(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY);
        sampleResponse.setPriority(Priority.MEDIUM);

        when(aiRecommendationService.overrideRecommendation(eq(1L), any(AiOverrideRequest.class), any()))
                .thenReturn(sampleResponse);

        AiOverrideRequest request = new AiOverrideRequest(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY, Priority.MEDIUM, 5L, "Incorrect category detected", "Overridden by admin");

        mockMvc.perform(post("/api/v1/admin/complaints/1/override-recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("AI recommendation overridden successfully by administrator"))
                .andExpect(jsonPath("$.data.category").value("STREETLIGHTS_AND_ELECTRICITY"));
    }

    @Test
    @DisplayName("POST and PUT /api/v1/admin/complaints/{id}/close returns closed complaint")
    void testCloseComplaint_Success() throws Exception {
        ComplaintResponse closed = ComplaintResponse.builder()
                .id(1L)
                .status(ComplaintStatus.CLOSED)
                .closureRemarks("Officially inspected and closed.")
                .build();

        when(lifecycleService.closeComplaint(eq(1L), any(CloseComplaintRequest.class), any()))
                .thenReturn(closed);

        CloseComplaintRequest request = new CloseComplaintRequest("Officially inspected and closed.");

        mockMvc.perform(post("/api/v1/admin/complaints/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.closureRemarks").value("Officially inspected and closed."));

        mockMvc.perform(put("/api/v1/admin/complaints/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
