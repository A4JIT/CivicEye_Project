package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.CloseComplaintRequest;
import com.civiceye.ai.complaint.dto.ComplaintFeedbackRequest;
import com.civiceye.ai.complaint.dto.ComplaintFeedbackResponse;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.ConfirmResolutionRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ComplaintClosureLifecycleTest {

    @Autowired
    private ComplaintLifecycleService lifecycleService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ComplaintFeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    private User citizen1;
    private User citizen2;
    private User admin;
    private User officer;
    private Department roadDept;
    private Complaint resolvedComplaint;
    private Complaint inProgressComplaint;

    private UserPrincipal citizen1Principal;
    private UserPrincipal citizen2Principal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal officerPrincipal;

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        citizen1 = userRepository.save(User.builder()
                .username("citizen_anita")
                .email("anita@example.com")
                .password("encoded_pass")
                .fullName("Anita Sharma")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        citizen2 = userRepository.save(User.builder()
                .username("citizen_bob")
                .email("bob@example.com")
                .password("encoded_pass")
                .fullName("Bob Miller")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        admin = userRepository.save(User.builder()
                .username("admin_raj")
                .email("admin_raj@example.com")
                .password("encoded_pass")
                .fullName("Rajesh Admin")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        roadDept = departmentRepository.findByCode("ROAD_WORKS")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .name("Roads & Infrastructure")
                        .code("ROAD_WORKS")
                        .description("Handles roads, footpaths, bridges")
                        .isActive(true)
                        .build()));

        officer = userRepository.save(User.builder()
                .username("officer_patil")
                .email("patil@example.com")
                .password("encoded_pass")
                .fullName("Sunil Patil")
                .role(Role.DEPARTMENT_USER)
                .department(roadDept)
                .isActive(true)
                .build());

        citizen1Principal = UserPrincipal.create(citizen1);
        citizen2Principal = UserPrincipal.create(citizen2);
        adminPrincipal = UserPrincipal.create(admin);
        officerPrincipal = UserPrincipal.create(officer);

        resolvedComplaint = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-2026-9001")
                .title("Deep pothole near Shivaji Chowk")
                .description("Pothole damaging vehicle suspensions")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.HIGH)
                .location("Shivaji Chowk, Main Road")
                .citizen(citizen1)
                .department(roadDept)
                .assignedTo(officer)
                .startedWorkingAt(LocalDateTime.now().minusDays(2))
                .resolvedAt(LocalDateTime.now().minusHours(4))
                .resolutionNotes("Pothole patched and asphalt levelled cleanly.")
                .resolutionImageUrl("https://images.unsplash.com/photo-1541888946425-d0fbb1861593?w=800")
                .build());

        inProgressComplaint = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-2026-9002")
                .title("Broken sidewalk slab")
                .description("Pedestrian trip hazard")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.IN_PROGRESS)
                .priority(Priority.MEDIUM)
                .location("Cross Street 3")
                .citizen(citizen1)
                .department(roadDept)
                .assignedTo(officer)
                .startedWorkingAt(LocalDateTime.now().minusDays(1))
                .build());
    }

    @Test
    @DisplayName("Citizen should confirm resolution, setting status to CLOSED and citizenConfirmedResolution to true")
    void confirmResolution_Success() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Confirmed fixed properly, thank you.");
        ComplaintResponse response = lifecycleService.confirmResolution(resolvedComplaint.getId(), request, citizen1Principal);

        assertEquals(ComplaintStatus.CLOSED, response.getStatus());
        assertTrue(response.getCitizenConfirmedResolution());
        assertEquals("Confirmed fixed properly, thank you.", response.getClosureRemarks());
        assertNotNull(response.getClosedAt());

        // Verify database persistence
        Complaint updated = complaintRepository.findById(resolvedComplaint.getId()).orElseThrow();
        assertEquals(ComplaintStatus.CLOSED, updated.getStatus());
        assertTrue(updated.getCitizenConfirmedResolution());
        assertNotNull(updated.getClosedAt());
        assertEquals("Confirmed fixed properly, thank you.", updated.getClosureRemarks());

        // Verify status audit history
        List<ComplaintStatusHistory> history = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(resolvedComplaint.getId());
        assertFalse(history.isEmpty());
        ComplaintStatusHistory latest = history.get(history.size() - 1);
        assertEquals(ComplaintStatus.RESOLVED, latest.getFromStatus());
        assertEquals(ComplaintStatus.CLOSED, latest.getToStatus());
        assertEquals(citizen1.getId(), latest.getChangedBy().getId());
    }

    @Test
    @DisplayName("Citizen confirming resolution with star rating should simultaneously create ComplaintFeedback")
    void confirmResolution_WithRatingAndComments_Success() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Excellent and swift work!", 5);
        ComplaintResponse response = lifecycleService.confirmResolution(resolvedComplaint.getId(), request, citizen1Principal);

        assertEquals(ComplaintStatus.CLOSED, response.getStatus());
        assertTrue(response.getCitizenConfirmedResolution());

        // Verify feedback was persisted
        assertTrue(feedbackRepository.existsByComplaintId(resolvedComplaint.getId()));
        ComplaintFeedback feedback = feedbackRepository.findByComplaintId(resolvedComplaint.getId()).orElseThrow();
        assertEquals(5, feedback.getRating());
        assertEquals("Excellent and swift work!", feedback.getComments());
        assertEquals(citizen1.getId(), feedback.getCitizen().getId());
        assertEquals(roadDept.getId(), feedback.getDepartment().getId());
    }

    @Test
    @DisplayName("Should throw BadRequestException if citizen confirms resolution on an unresolved complaint")
    void confirmResolution_FailsIfNotResolved() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Attempt early closure");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            lifecycleService.confirmResolution(inProgressComplaint.getId(), request, citizen1Principal);
        });
        assertTrue(ex.getMessage().contains("Only resolved complaints can have their resolution confirmed"));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException if a different citizen tries to confirm resolution")
    void confirmResolution_FailsIfNotOwner() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Unauthorized confirmation");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.confirmResolution(resolvedComplaint.getId(), request, citizen2Principal);
        });
        assertTrue(ex.getMessage().contains("Only the reporting citizen can confirm resolution"));
    }

    @Test
    @DisplayName("Admin should successfully close resolved complaint with administrative closure remarks")
    void closeComplaint_AdminSuccess() {
        CloseComplaintRequest request = new CloseComplaintRequest("Verified by ward superintendent on-site inspection.");
        ComplaintResponse response = lifecycleService.closeComplaint(resolvedComplaint.getId(), request, adminPrincipal);

        assertEquals(ComplaintStatus.CLOSED, response.getStatus());
        assertEquals(admin.getId(), response.getClosedById());
        assertEquals(admin.getFullName(), response.getClosedByName());
        assertEquals("Verified by ward superintendent on-site inspection.", response.getClosureRemarks());
        assertNotNull(response.getClosedAt());

        Complaint updated = complaintRepository.findById(resolvedComplaint.getId()).orElseThrow();
        assertEquals(ComplaintStatus.CLOSED, updated.getStatus());
        assertEquals(admin.getId(), updated.getClosedBy().getId());
        assertNotNull(updated.getClosedAt());

        // Audit history check
        List<ComplaintStatusHistory> history = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(resolvedComplaint.getId());
        ComplaintStatusHistory latest = history.get(history.size() - 1);
        assertEquals(ComplaintStatus.RESOLVED, latest.getFromStatus());
        assertEquals(ComplaintStatus.CLOSED, latest.getToStatus());
        assertEquals(admin.getId(), latest.getChangedBy().getId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when non-admin attempts administrative closure")
    void closeComplaint_FailsIfNotAdmin() {
        CloseComplaintRequest request = new CloseComplaintRequest("Officer closing complaint");
        assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.closeComplaint(resolvedComplaint.getId(), request, officerPrincipal);
        });

        assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.closeComplaint(resolvedComplaint.getId(), request, citizen1Principal);
        });
    }

    @Test
    @DisplayName("Admin closure should fail if complaint is not in RESOLVED status")
    void closeComplaint_FailsIfNotResolved() {
        CloseComplaintRequest request = new CloseComplaintRequest("Premature admin close");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            lifecycleService.closeComplaint(inProgressComplaint.getId(), request, adminPrincipal);
        });
        assertTrue(ex.getMessage().contains("Only complaints in RESOLVED status can be closed"));
    }

    @Test
    @DisplayName("Should successfully submit feedback for resolved complaint")
    void submitFeedback_Success() {
        ComplaintFeedbackRequest request = new ComplaintFeedbackRequest(5, "Very impressed by the quick action!");
        ComplaintFeedbackResponse response = lifecycleService.submitFeedback(resolvedComplaint.getId(), request, citizen1Principal);

        assertNotNull(response.getId());
        assertEquals(resolvedComplaint.getId(), response.getComplaintId());
        assertEquals(5, response.getRating());
        assertEquals("Very impressed by the quick action!", response.getComments());
        assertEquals(citizen1.getId(), response.getCitizenId());
        assertEquals(roadDept.getId(), response.getDepartmentId());

        assertTrue(feedbackRepository.existsByComplaintId(resolvedComplaint.getId()));
    }

    @Test
    @DisplayName("Should allow feedback submission on already CLOSED complaints")
    void submitFeedback_AllowedOnClosedComplaint() {
        resolvedComplaint.setStatus(ComplaintStatus.CLOSED);
        resolvedComplaint.setClosedAt(LocalDateTime.now());
        complaintRepository.save(resolvedComplaint);

        ComplaintFeedbackRequest request = new ComplaintFeedbackRequest(4, "Lighting works well now.");
        ComplaintFeedbackResponse response = lifecycleService.submitFeedback(resolvedComplaint.getId(), request, citizen1Principal);

        assertEquals(4, response.getRating());
        assertTrue(feedbackRepository.existsByComplaintId(resolvedComplaint.getId()));
    }

    @Test
    @DisplayName("Should throw BadRequestException when submitting feedback on complaint not RESOLVED or CLOSED")
    void submitFeedback_FailsIfNotResolvedOrClosed() {
        ComplaintFeedbackRequest request = new ComplaintFeedbackRequest(5, "Premature feedback");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            lifecycleService.submitFeedback(inProgressComplaint.getId(), request, citizen1Principal);
        });
        assertTrue(ex.getMessage().contains("Feedback can only be submitted for complaints in RESOLVED or CLOSED status"));
    }

    @Test
    @DisplayName("Should prevent duplicate feedback submission for the same complaint")
    void submitFeedback_FailsIfDuplicate() {
        ComplaintFeedbackRequest first = new ComplaintFeedbackRequest(4, "Good service");
        lifecycleService.submitFeedback(resolvedComplaint.getId(), first, citizen1Principal);

        ComplaintFeedbackRequest second = new ComplaintFeedbackRequest(5, "Trying duplicate");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            lifecycleService.submitFeedback(resolvedComplaint.getId(), second, citizen1Principal);
        });
        assertTrue(ex.getMessage().contains("Feedback has already been submitted"));
    }

    @Test
    @DisplayName("Should reject feedback ratings outside the 1 to 5 star range")
    void submitFeedback_FailsIfInvalidRating() {
        ComplaintFeedbackRequest tooLow = new ComplaintFeedbackRequest(0, "Zero stars");
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> {
            lifecycleService.submitFeedback(resolvedComplaint.getId(), tooLow, citizen1Principal);
        });
        assertTrue(ex1.getMessage().contains("Rating must be between 1 and 5"));

        ComplaintFeedbackRequest tooHigh = new ComplaintFeedbackRequest(6, "Six stars");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> {
            lifecycleService.submitFeedback(resolvedComplaint.getId(), tooHigh, citizen1Principal);
        });
        assertTrue(ex2.getMessage().contains("Rating must be between 1 and 5"));
    }
}
