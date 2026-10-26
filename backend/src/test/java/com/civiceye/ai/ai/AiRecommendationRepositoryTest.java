package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.Complaint;
import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.ComplaintRepository;
import com.civiceye.ai.complaint.ComplaintStatus;
import com.civiceye.ai.complaint.Priority;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("AiRecommendation JPA Entity & Repository Integration Tests")
class AiRecommendationRepositoryTest {

    @Autowired
    private AiRecommendationRepository aiRecommendationRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User citizen;
    private User admin;
    private Department roadDept;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        aiRecommendationRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        citizen = userRepository.save(User.builder()
                .username("jane_citizen")
                .email("jane@civiceye.com")
                .password("encoded_pass")
                .fullName("Jane Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        admin = userRepository.save(User.builder()
                .username("admin_triage")
                .email("triage@civiceye.com")
                .password("encoded_pass")
                .fullName("Admin Triage")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        roadDept = departmentRepository.save(Department.builder()
                .name("Public Works & Roads")
                .code("ROAD_WORKS")
                .contactEmail("roads@civiceye.gov.in")
                .isActive(true)
                .build());

        complaint = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-AI-TEST-001")
                .title("Collapsed bridge span on highway")
                .description("Dangerous collapse of bridge structure creating traffic hazard")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.CRITICAL)
                .location("Outer Highway KM 12")
                .citizen(citizen)
                .build());
    }

    @Test
    @DisplayName("Persists and retrieves AiRecommendation with confidence score and explanation")
    void testSaveAndRetrieveRecommendation() {
        AiRecommendation rec = AiRecommendation.builder()
                .complaint(complaint)
                .suggestedCategory(ComplaintCategory.ROADS_AND_FOOTPATHS.name())
                .suggestedPriority(Priority.CRITICAL.name())
                .suggestedDepartment(roadDept)
                .confidenceScore(0.96)
                .explanation("Classified as ROADS_AND_FOOTPATHS based on critical keyword [collapsed bridge]")
                .isOverridden(false)
                .build();

        AiRecommendation saved = aiRecommendationRepository.save(rec);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        Optional<AiRecommendation> found = aiRecommendationRepository.findByComplaintId(complaint.getId());
        assertTrue(found.isPresent());
        assertEquals("ROADS_AND_FOOTPATHS", found.get().getSuggestedCategory());
        assertEquals("CRITICAL", found.get().getSuggestedPriority());
        assertEquals(0.96, found.get().getConfidenceScore());
        assertEquals("ROAD_WORKS", found.get().getSuggestedDepartment().getCode());
        assertFalse(found.get().getIsOverridden());
    }

    @Test
    @DisplayName("Records administrative override audit metadata cleanly")
    void testAdminOverrideAuditTrail() {
        AiRecommendation rec = aiRecommendationRepository.save(AiRecommendation.builder()
                .complaint(complaint)
                .suggestedCategory(ComplaintCategory.ROADS_AND_FOOTPATHS.name())
                .suggestedPriority(Priority.CRITICAL.name())
                .suggestedDepartment(roadDept)
                .confidenceScore(0.85)
                .explanation("Initial AI suggestion")
                .isOverridden(false)
                .build());

        // Perform override
        rec.setIsOverridden(true);
        rec.setOverriddenBy(admin);
        rec.setOverriddenAt(LocalDateTime.now());
        rec.setOverrideReason("Administrator reassigned priority based on emergency site verification");
        aiRecommendationRepository.save(rec);

        AiRecommendation updated = aiRecommendationRepository.findById(rec.getId()).orElseThrow();
        assertTrue(updated.getIsOverridden());
        assertNotNull(updated.getOverriddenBy());
        assertEquals("triage@civiceye.com", updated.getOverriddenBy().getEmail());
        assertNotNull(updated.getOverriddenAt());
        assertTrue(updated.getOverrideReason().contains("emergency site verification"));

        List<AiRecommendation> overriddenList = aiRecommendationRepository.findByIsOverridden(true);
        assertEquals(1, overriddenList.size());
    }

    @Test
    @DisplayName("Enforces cascade deletion: deleting a complaint removes associated AiRecommendation")
    void testCascadeDeleteWithComplaint() {
        AiRecommendation rec = aiRecommendationRepository.save(AiRecommendation.builder()
                .complaint(complaint)
                .suggestedCategory(ComplaintCategory.ROADS_AND_FOOTPATHS.name())
                .suggestedPriority(Priority.CRITICAL.name())
                .suggestedDepartment(roadDept)
                .confidenceScore(0.90)
                .explanation("Suggestion to be deleted")
                .isOverridden(false)
                .build());

        Long recId = rec.getId();
        complaint.setAiRecommendation(rec);
        complaint = complaintRepository.saveAndFlush(complaint);
        assertTrue(aiRecommendationRepository.existsById(recId));

        complaintRepository.delete(complaint);
        complaintRepository.flush();

        assertFalse(aiRecommendationRepository.existsById(recId));
    }
}
