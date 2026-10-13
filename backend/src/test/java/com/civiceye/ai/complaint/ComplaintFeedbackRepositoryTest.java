package com.civiceye.ai.complaint;

import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    private User citizen;
    private Complaint complaint1;
    private Complaint complaint2;

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();

        citizen = userRepository.save(User.builder()
                .username("testcitizen")
                .email("citizen@example.com")
                .password("encoded_pass")
                .fullName("Test Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        complaint1 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-FB-0001")
                .title("Street light fixture failure")
                .description("Light fixture on 5th avenue is completely non-functional")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.MEDIUM)
                .location("5th Avenue, Cross 2")
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
                .citizen(citizen)
                .build());
    }

    @Test
    @DisplayName("Should successfully save and retrieve citizen feedback for resolved complaint")
    void saveAndFindFeedback_Success() {
        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .complaint(complaint1)
                .citizen(citizen)
                .rating(5)
                .comments("Repair completed very quickly. Highly satisfied!")
                .build();

        ComplaintFeedback saved = feedbackRepository.save(feedback);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(5, saved.getRating());
        assertEquals("Repair completed very quickly. Highly satisfied!", saved.getComments());

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
                .rating(4)
                .comments("Good job")
                .build());

        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint2)
                .citizen(citizen)
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
                .rating(4)
                .comments("Pretty good")
                .build());

        feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint2)
                .citizen(citizen)
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
}
