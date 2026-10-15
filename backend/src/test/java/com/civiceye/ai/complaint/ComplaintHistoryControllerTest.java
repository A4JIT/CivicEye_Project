package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintAssignmentResponse;
import com.civiceye.ai.complaint.dto.ComplaintTimelineResponse;
import com.civiceye.ai.complaint.dto.TimelineEventDto;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import org.springframework.security.access.AccessDeniedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComplaintHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplaintService complaintService;

    @Mock
    private ComplaintLifecycleService lifecycleService;

    @Mock
    private AdminComplaintService adminComplaintService;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private ComplaintHistoryService historyService;

    @InjectMocks
    private ComplaintController complaintController;

    private UserPrincipal testCitizenPrincipal;

    @BeforeEach
    void setUp() {
        User citizenUser = User.builder()
                .id(10L)
                .email("citizen@civiceye.com")
                .username("citizen")
                .fullName("Citizen Jane")
                .role(Role.CITIZEN)
                .isActive(true)
                .build();
        testCitizenPrincipal = UserPrincipal.create(citizenUser);

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testCitizenPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(complaintController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/complaints/{id}/history returns 200 with full chronological timeline")
    void getComplaintHistory_Returns200() throws Exception {
        ComplaintTimelineResponse response = ComplaintTimelineResponse.builder()
                .complaintId(101L)
                .complaintNumber("CE-2026-1003")
                .title("Major Water Main Rupture")
                .currentStatus(ComplaintStatus.CLOSED)
                .priority(Priority.CRITICAL)
                .events(List.of(
                        TimelineEventDto.builder()
                                .id(1L)
                                .timestamp(LocalDateTime.now().minusDays(2))
                                .eventType("SUBMITTED")
                                .actorRole("CITIZEN")
                                .actorName("Citizen Jane")
                                .title("Complaint Filed")
                                .remarks("Emergency leak report")
                                .build(),
                        TimelineEventDto.builder()
                                .id(2L)
                                .timestamp(LocalDateTime.now().minusDays(1))
                                .eventType("RESOLVED")
                                .actorRole("DEPARTMENT_USER")
                                .actorName("Officer Sharma")
                                .title("Resolved")
                                .remarks("Main pipe replaced")
                                .evidenceUrl("https://images.example.com/resolved.jpg")
                                .build()
                ))
                .build();

        when(historyService.getComplaintHistory(eq(101L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/complaints/101/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintId").value(101))
                .andExpect(jsonPath("$.data.complaintNumber").value("CE-2026-1003"))
                .andExpect(jsonPath("$.data.currentStatus").value("CLOSED"))
                .andExpect(jsonPath("$.data.events").isArray())
                .andExpect(jsonPath("$.data.events[0].eventType").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.events[1].eventType").value("RESOLVED"));
    }

    @Test
    @DisplayName("GET /api/v1/complaints/{id}/assignments returns 200 with assignment tracking list")
    void getComplaintAssignments_Returns200() throws Exception {
        List<ComplaintAssignmentResponse> assignments = List.of(
                ComplaintAssignmentResponse.builder()
                        .id(1L)
                        .complaintId(101L)
                        .departmentId(5L)
                        .departmentName("Roads Maintenance")
                        .assignedToName("Officer Sharma")
                        .isActive(false)
                        .instructions("Initial paving inspection")
                        .build(),
                ComplaintAssignmentResponse.builder()
                        .id(2L)
                        .complaintId(101L)
                        .departmentId(6L)
                        .departmentName("Water Supply")
                        .assignedToName("Officer Verma")
                        .isActive(true)
                        .instructions("Repair pipe leak")
                        .build()
        );

        when(historyService.getAssignmentHistory(eq(101L), any())).thenReturn(assignments);

        mockMvc.perform(get("/api/v1/complaints/101/assignments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].departmentName").value("Roads Maintenance"))
                .andExpect(jsonPath("$.data[1].departmentName").value("Water Supply"));
    }

    @Test
    @DisplayName("GET /api/v1/complaints/{id}/history returns 403 when access is denied by role boundary")
    void getComplaintHistory_AccessDenied_Returns403() throws Exception {
        when(historyService.getComplaintHistory(eq(999L), any()))
                .thenThrow(new AccessDeniedException("Access denied: You can only view history for your own complaints"));

        mockMvc.perform(get("/api/v1/complaints/999/history"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/complaints/{id}/history returns 404 when complaint does not exist")
    void getComplaintHistory_NotFound_Returns404() throws Exception {
        when(historyService.getComplaintHistory(eq(404L), any()))
                .thenThrow(new ResourceNotFoundException("Complaint not found with id: 404"));

        mockMvc.perform(get("/api/v1/complaints/404/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
