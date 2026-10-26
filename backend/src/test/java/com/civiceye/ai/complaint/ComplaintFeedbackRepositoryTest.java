package com.civiceye.ai.complaint;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ComplaintFeedbackRepositoryTest {

    @Autowired
    private ComplaintFeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User citizen;
    private Department roadDept;
    private Department elecDept;
    private Complaint complaint1;
    private Complaint complaint2;

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        citizen = userRepository.save(User.builder()
                .username("testcitizen")
                .email("citizen@example.com")
                .password("encoded_pass")
                .fullName("Test Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        roadDept = departmentRepository.findByCode("ROAD_WORKS")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .name("Roads & Infrastructure")
                        .code("ROAD_WORKS")
                        .description("Road maintenance")
                        .isActive(true)
                        .build()));

        elecDept = departmentRepository.findByCode("ELECTRICITY")
                .orElseGet(() -> departmentRepository.save(Department.builder()
                        .name("Streetlights & Electricity")
                        .code("ELECTRICITY")
                        .description("Lighting maintenance")
                        .isActive(true)
                        .build()));

        complaint1 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-FB-0001")
                .title("Street light fixture failure")
                .description("Light fixture on 5th avenue is completely non-functional")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.MEDIUM)
                .location("5th Avenue, Cross 2")
                .department(elecDept)
                .citizen(citizen)
                .build());

        complaint2 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-FB-0002")
                .title("Road pothole after storm")
                .description("Large pothole near intersection")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.HIGH)
                .location("Market Road")
                .department(roadDept)
                .citizen(citizen)
                .build());
    }

    @Test
    @DisplayName("Should successfully save and retrieve citizen feedback for resolved complaint")
    void saveAndFindFeedback_Success() {
        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(5)
                .comments("Repair completed very quickly. Highly satisfied!")
                .build();

        ComplaintFeedback saved = feedbackRepository.save(feedback);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(5, saved.getRating());
        assertEquals("Repair completed very quickly. Highly satisfied!", saved.getComments());
        assertEquals(elecDept.getId(), saved.getDepartment().getId());

        Optional<ComplaintFeedback> retrieved = feedbackRepository.findByComplaintId(complaint1.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(5, retrieved.get().getRating());
        assertTrue(feedbackRepository.existsByComplaintId(complaint1.getId()));
    }

    @Test
    @DisplayName("Should retrieve feedback list by citizen ordered by creation date descending")
    void findByCitizenId_Success() {
        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(4)
                .comments("Good job")
                .build());

        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint2)
                .citizen(citizen)
                .department(roadDept)
                .rating(5)
                .comments("Exceptional response speed")
                .build());

        List<ComplaintFeedback> citizenFeedbacks = feedbackRepository.findByCitizenIdOrderByCreatedAtDesc(citizen.getId());
        assertEquals(2, citizenFeedbacks.size());
    }

    @Test
    @DisplayName("Should accurately calculate average satisfaction rating across multiple feedback entries")
    void calculateAverageSatisfactionRating_Success() {
        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(4)
                .comments("Pretty good")
                .build());

        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint2)
                .citizen(citizen)
                .department(roadDept)
                .rating(5)
                .comments("Flawless")
                .build());

        Double avgRating = feedbackRepository.getAverageSatisfactionRating();
        assertNotNull(avgRating);
        assertEquals(4.5, avgRating, 0.001);
        assertEquals(1, feedbackRepository.countByRating(5));
        assertEquals(1, feedbackRepository.countByRating(4));
        assertEquals(0, feedbackRepository.countByRating(1));
    }

    @Test
    @DisplayName("Should accurately calculate average satisfaction rating per department")
    void calculateDepartmentAverageRating_Success() {
        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(4)
                .comments("Good streetlight fix")
                .build());

        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint2)
                .citizen(citizen)
                .department(roadDept)
                .rating(5)
                .comments("Superb road fix")
                .build());

        Double elecAvg = feedbackRepository.getAverageSatisfactionRatingByDepartmentId(elecDept.getId());
        Double roadAvg = feedbackRepository.getAverageSatisfactionRatingByDepartmentId(roadDept.getId());

        assertEquals(4.0, elecAvg, 0.001);
        assertEquals(5.0, roadAvg, 0.001);
        assertEquals(1, feedbackRepository.countByDepartmentId(elecDept.getId()));
        assertEquals(1, feedbackRepository.countByDepartmentId(roadDept.getId()));

        List<ComplaintFeedback> roadFeedbacks = feedbackRepository.findByDepartmentIdOrderByCreatedAtDesc(roadDept.getId());
        assertEquals(1, roadFeedbacks.size());
        assertEquals(5, roadFeedbacks.get(0).getRating());
    }

    @Test
    @DisplayName("Should enforce unique constraint preventing multiple feedback entries on same complaint")
    void duplicateFeedback_ThrowsDataIntegrityViolation() {
        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(4)
                .comments("First feedback")
                .build());

        ComplaintFeedback duplicate = ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .department(elecDept)
                .rating(5)
                .comments("Duplicate feedback attempt")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            feedbackRepository.saveAndFlush(duplicate);
        });
    }
}
