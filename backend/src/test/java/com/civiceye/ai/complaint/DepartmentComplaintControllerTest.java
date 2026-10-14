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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepartmentComplaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DepartmentComplaintService departmentComplaintService;

    @InjectMocks
    private DepartmentComplaintController departmentComplaintController;

    private ObjectMapper objectMapper;
    private ComplaintResponse sampleResponse;
    private DepartmentComplaintDetailResponse sampleDetailResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentComplaintController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleResponse = ComplaintResponse.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Severe pothole damaging vehicles")
                .description("Crater near pillar 42")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.ASSIGNED)
                .priority(Priority.HIGH)
                .location("MG Road Junction")
                .citizenId(10L)
                .citizenName("Jane Citizen")
                .departmentId(1L)
                .departmentName("Public Works & Roads")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleDetailResponse = new DepartmentComplaintDetailResponse();
        sampleDetailResponse.setId(100L);
        sampleDetailResponse.setComplaintNumber("CE-2026-0001");
        sampleDetailResponse.setTitle("Severe pothole damaging vehicles");
        sampleDetailResponse.setDescription("Crater near pillar 42");
        sampleDetailResponse.setCategory(ComplaintCategory.ROADS_AND_FOOTPATHS);
        sampleDetailResponse.setStatus(ComplaintStatus.ASSIGNED);
        sampleDetailResponse.setPriority(Priority.HIGH);
        sampleDetailResponse.setLocation("MG Road Junction");
        sampleDetailResponse.setDepartmentId(1L);
        sampleDetailResponse.setDepartmentName("Public Works & Roads");
        sampleDetailResponse.setTimeline(Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/department/complaints returns paginated complaints")
    void testGetDepartmentComplaints_Success() throws Exception {
        Page<ComplaintResponse> page = new PageImpl<>(List.of(sampleResponse));
        when(departmentComplaintService.searchDepartmentComplaints(any(), any(), any(), any(), any(Pageable.class), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/department/complaints")
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
    @DisplayName("GET /api/department/complaints/{id} returns complaint details and timeline")
    void testGetDepartmentComplaintById_Success() throws Exception {
        when(departmentComplaintService.getDepartmentComplaintById(eq(100L), any()))
                .thenReturn(sampleDetailResponse);

        mockMvc.perform(get("/api/department/complaints/100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.complaintNumber").value("CE-2026-0001"))
                .andExpect(jsonPath("$.data.departmentName").value("Public Works & Roads"))
                .andExpect(jsonPath("$.data.timeline").isArray());
    }

    @Test
    @DisplayName("PUT /api/department/complaints/{id}/start-work transitions complaint to IN_PROGRESS")
    void testStartWork_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleResponse.setProgressNotes("Dispatched repair crew");
        sampleResponse.setStartedWorkingAt(LocalDateTime.now());

        when(departmentComplaintService.startWork(eq(100L), any(), any()))
                .thenReturn(sampleResponse);

        StartWorkRequest request = new StartWorkRequest("Dispatched repair crew");

        mockMvc.perform(put("/api/department/complaints/100/start-work")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.progressNotes").value("Dispatched repair crew"));
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/progress records progress note")
    void testAddProgressNote_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleResponse.setProgressNotes("Aggregate base layer applied");

        when(departmentComplaintService.addProgressNote(eq(100L), any(), any()))
                .thenReturn(sampleResponse);

        ProgressNoteRequest request = new ProgressNoteRequest("Aggregate base layer applied");

        mockMvc.perform(post("/api/department/complaints/100/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.progressNotes").value("Aggregate base layer applied"));
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/progress with blank notes returns 400 Bad Request")
    void testAddProgressNote_BlankNotes_ReturnsBadRequest() throws Exception {
        ProgressNoteRequest request = new ProgressNoteRequest("");

        mockMvc.perform(post("/api/department/complaints/100/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/resolve via JSON resolves complaint")
    void testResolveComplaintJson_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.RESOLVED);
        sampleResponse.setResolutionNotes("Pothole patched with asphalt and rolled flat");
        sampleResponse.setResolvedAt(LocalDateTime.now());

        when(departmentComplaintService.resolveComplaint(eq(100L), any(), any(), any()))
                .thenReturn(sampleResponse);

        ResolveComplaintRequest request = new ResolveComplaintRequest("Pothole patched with asphalt and rolled flat");

        mockMvc.perform(post("/api/department/complaints/100/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolutionNotes").value("Pothole patched with asphalt and rolled flat"));
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/resolve via JSON with blank notes returns 400 Bad Request")
    void testResolveComplaintJson_BlankNotes_ReturnsBadRequest() throws Exception {
        ResolveComplaintRequest request = new ResolveComplaintRequest("");

        mockMvc.perform(post("/api/department/complaints/100/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/resolve via Multipart resolves complaint with proof image")
    void testResolveComplaintMultipart_Success() throws Exception {
        sampleResponse.setStatus(ComplaintStatus.RESOLVED);
        sampleResponse.setResolutionNotes("Pothole fully repaired");
        sampleResponse.setResolutionImageUrl("/api/files/resolved-proof.jpg");
        sampleResponse.setResolvedAt(LocalDateTime.now());

        MockMultipartFile proofFile = new MockMultipartFile(
                "image",
                "proof.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        when(departmentComplaintService.resolveComplaint(eq(100L), any(), any(), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(multipart("/api/department/complaints/100/resolve")
                        .file(proofFile)
                        .param("resolutionNotes", "Pothole fully repaired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolutionNotes").value("Pothole fully repaired"))
                .andExpect(jsonPath("$.data.resolutionImageUrl").value("/api/files/resolved-proof.jpg"));
    }

    @Test
    @DisplayName("POST /api/department/complaints/{id}/resolve via Multipart with blank notes returns 400 Bad Request")
    void testResolveComplaintMultipart_BlankNotes_ReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/department/complaints/100/resolve")
                        .param("resolutionNotes", ""))
                .andExpect(status().isBadRequest());
    }
}
