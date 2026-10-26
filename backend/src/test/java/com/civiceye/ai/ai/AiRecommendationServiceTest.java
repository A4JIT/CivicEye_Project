package com.civiceye.ai.ai;

import com.civiceye.ai.ai.dto.AiOverrideRequest;
import com.civiceye.ai.ai.dto.AiRecommendRequest;
import com.civiceye.ai.ai.dto.AiRecommendationResponse;
import com.civiceye.ai.complaint.*;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiRecommendationService Unit Tests")
class AiRecommendationServiceTest {

    @Mock
    private DepartmentRecommendationService departmentRecommendationService;

    @Mock
    private AiRecommendationRepository aiRecommendationRepository;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiRecommendationService aiRecommendationService;

    private User sampleCitizen;
    private User adminUser;
    private User roadsOfficer;
    private UserPrincipal citizenPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal officerPrincipal;
    private Department roadsDept;
    private Department wasteDept;
    private Complaint sampleComplaint;
    private AiRecommendation sampleRecommendation;

    @BeforeEach
    void setUp() {
        roadsDept = Department.builder()
                .id(1L)
                .code("ROAD_WORKS")
                .name("Public Works & Roads")
                .build();

        wasteDept = Department.builder()
                .id(2L)
                .code("SOLID_WASTE")
                .name("Sanitation & Waste Management")
                .build();

        sampleCitizen = User.builder()
                .id(101L)
                .username("jane_citizen")
                .email("citizen@civiceye.com")
                .role(Role.CITIZEN)
                .build();

        adminUser = User.builder()
                .id(1L)
                .username("super_admin")
                .email("admin@civiceye.com")
                .role(Role.ADMIN)
                .build();

        roadsOfficer = User.builder()
                .id(201L)
                .username("officer_roads")
                .email("roads@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(roadsDept)
                .build();

        citizenPrincipal = new UserPrincipal(
                sampleCitizen.getId(), sampleCitizen.getUsername(), sampleCitizen.getEmail(),
                "password", "Jane Citizen", Role.CITIZEN, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
        );

        adminPrincipal = new UserPrincipal(
                adminUser.getId(), adminUser.getUsername(), adminUser.getEmail(),
                "password", "Admin User", Role.ADMIN, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        officerPrincipal = new UserPrincipal(
                roadsOfficer.getId(), roadsOfficer.getUsername(), roadsOfficer.getEmail(),
                "password", "Roads Officer", Role.DEPARTMENT_USER, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEPARTMENT_USER"))
        );

        sampleComplaint = Complaint.builder()
                .id(501L)
                .complaintNumber("CE-2026-0501")
                .title("Collapsed bridge section on outer highway")
                .description("Severe bridge collapse creating immediate danger")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .priority(Priority.CRITICAL)
                .status(ComplaintStatus.SUBMITTED)
                .citizen(sampleCitizen)
                .department(roadsDept)
                .build();

        sampleRecommendation = AiRecommendation.builder()
                .id(10L)
                .complaint(sampleComplaint)
                .suggestedCategory(ComplaintCategory.ROADS_AND_FOOTPATHS.name())
                .suggestedPriority(Priority.CRITICAL.name())
                .suggestedDepartment(roadsDept)
                .confidenceScore(0.96)
                .explanation("Classified as ROADS_AND_FOOTPATHS based on critical hazard: [collapsed bridge]")
                .isOverridden(false)
                .build();
    }

    @Test
    @DisplayName("Calculates real-time recommendation within <50ms without models")
    void testCalculateRecommendation() {
        AiRecommendRequest request = new AiRecommendRequest("Deep pothole on flyover", "Asphalt peeling on road", "Flyover Lane 2");

        when(departmentRecommendationService.buildFullRecommendation(request.getTitle(), request.getDescription(), request.getLocation()))
                .thenReturn(sampleRecommendation);

        long start = System.currentTimeMillis();
        AiRecommendationResponse res = aiRecommendationService.calculateRecommendation(request);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 50, "Rule engine calculation must execute in under 50ms");
        assertNotNull(res);
        assertEquals("ROADS_AND_FOOTPATHS", res.getSuggestedCategory());
        assertEquals("CRITICAL", res.getSuggestedPriority());
        assertEquals("ROAD_WORKS", res.getSuggestedDepartmentCode());
        assertEquals(0.96, res.getConfidenceScore());
    }

    @Test
    @DisplayName("Rejects calculateRecommendation when title is blank")
    void testCalculateRecommendationBlankTitle() {
        AiRecommendRequest request = new AiRecommendRequest("", "details", "loc");
        assertThrows(BadRequestException.class, () -> aiRecommendationService.calculateRecommendation(request));
    }

    @Test
    @DisplayName("Retrieves stored AI recommendation for citizen who owns the complaint")
    void testGetComplaintRecommendationCitizenOwner() {
        when(complaintRepository.findById(501L)).thenReturn(Optional.of(sampleComplaint));
        when(aiRecommendationRepository.findByComplaintId(501L)).thenReturn(Optional.of(sampleRecommendation));

        AiRecommendationResponse res = aiRecommendationService.getComplaintRecommendation(501L, citizenPrincipal);
        assertNotNull(res);
        assertEquals("ROADS_AND_FOOTPATHS", res.getSuggestedCategory());
    }

    @Test
    @DisplayName("Prevents unauthorized citizen from accessing another citizen's complaint recommendation")
    void testGetComplaintRecommendationUnauthorizedCitizen() {
        UserPrincipal otherCitizen = new UserPrincipal(
                999L, "other", "other@example.com", "pass", "Other", Role.CITIZEN, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
        );

        when(complaintRepository.findById(501L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(UnauthorizedException.class, () ->
                aiRecommendationService.getComplaintRecommendation(501L, otherCitizen));
    }

    @Test
    @DisplayName("Admin applies AI recommendation in 1-click triage workflow")
    void testApplyRecommendationSuccess() {
        when(complaintRepository.findById(501L)).thenReturn(Optional.of(sampleComplaint));
        when(aiRecommendationRepository.findByComplaintId(501L)).thenReturn(Optional.of(sampleRecommendation));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));

        ComplaintResponse response = aiRecommendationService.applyRecommendation(501L, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.ASSIGNED, response.getStatus());
        assertEquals(ComplaintCategory.ROADS_AND_FOOTPATHS, response.getCategory());
        assertEquals(Priority.CRITICAL, response.getPriority());
        assertFalse(response.getAiCategoryOverridden());
        assertFalse(response.getAiPriorityOverridden());

        verify(statusHistoryRepository).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Non-admin user cannot apply recommendation")
    void testApplyRecommendationForbiddenForCitizen() {
        assertThrows(UnauthorizedException.class, () ->
                aiRecommendationService.applyRecommendation(501L, citizenPrincipal));
    }

    @Test
    @DisplayName("Admin overrides AI recommendation with custom category and reason")
    void testOverrideRecommendationSuccess() {
        when(complaintRepository.findById(501L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(adminPrincipal.getId())).thenReturn(Optional.of(adminUser));
        when(aiRecommendationRepository.findByComplaintId(501L)).thenReturn(Optional.of(sampleRecommendation));
        when(departmentRepository.findById(2L)).thenReturn(Optional.of(wasteDept));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        AiOverrideRequest overrideReq = AiOverrideRequest.builder()
                .category(ComplaintCategory.GARBAGE_AND_CLEANLINESS)
                .priority(Priority.HIGH)
                .departmentId(2L)
                .reason("Re-classified to sanitation department following on-site officer inspection")
                .adminNotes("Urgent waste removal required")
                .build();

        ComplaintResponse response = aiRecommendationService.overrideRecommendation(501L, overrideReq, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintCategory.GARBAGE_AND_CLEANLINESS, response.getCategory());
        assertEquals(Priority.HIGH, response.getPriority());
        assertTrue(response.getAiCategoryOverridden());
        assertTrue(response.getAiPriorityOverridden());

        verify(aiRecommendationRepository).save(argThat(AiRecommendation::getIsOverridden));
        verify(statusHistoryRepository).save(any(ComplaintStatusHistory.class));
    }
}
