package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.FeedbackResponse;
import com.civiceye.ai.complaint.dto.FeedbackStatsResponse;
import com.civiceye.ai.complaint.dto.SubmitFeedbackRequest;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintFeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User citizen1;
    private User citizen2;
    private Department roadDept;
    private Complaint resolvedComplaint;
    private Complaint inProgressComplaint;
    private UserPrincipal citizen1Principal;
    private UserPrincipal citizen2Principal;

    @BeforeEach
    void setUp() {
        citizen1 = User.builder()
                .id(1L)
                .username("citizen1")
                .email("c1@example.com")
                .fullName("Citizen One")
                .role(Role.CITIZEN)
                .build();

        citizen2 = User.builder()
                .id(2L)
                .username("citizen2")
                .email("c2@example.com")
                .fullName("Citizen Two")
                .role(Role.CITIZEN)
                .build();

        roadDept = Department.builder()
                .id(10L)
                .name("Roads & Infrastructure")
                .code("ROAD_WORKS")
                .build();

        resolvedComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Road Pothole")
                .status(ComplaintStatus.RESOLVED)
                .citizen(citizen1)
                .department(roadDept)
                .build();

        inProgressComplaint = Complaint.builder()
                .id(200L)
                .complaintNumber("CE-2026-0002")
                .title("Streetlight Out")
                .status(ComplaintStatus.IN_PROGRESS)
                .citizen(citizen1)
                .department(roadDept)
                .build();

        citizen1Principal = UserPrincipal.create(citizen1);
        citizen2Principal = UserPrincipal.create(citizen2);
    }

    @Test
    @DisplayName("Should submit feedback successfully for resolved complaint")
    void submitFeedback_Success() {
        SubmitFeedbackRequest request = new SubmitFeedbackRequest(5, "Excellent service!");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(feedbackRepository.existsByComplaintId(100L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen1));

        ComplaintFeedback saved = ComplaintFeedback.builder()
                .id(1L)
                .complaint(resolvedComplaint)
                .citizen(citizen1)
                .department(roadDept)
                .rating(5)
                .comments("Excellent service!")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.save(any(ComplaintFeedback.class))).thenReturn(saved);

        FeedbackResponse response = feedbackService.submitFeedback(100L, request, citizen1Principal);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Excellent service!", response.getComments());
        assertEquals(100L, response.getComplaintId());
        verify(complaintRepository).save(resolvedComplaint);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when a different citizen attempts to submit feedback")
    void submitFeedback_FailsIfNotOwner() {
        SubmitFeedbackRequest request = new SubmitFeedbackRequest(5, "Unauthorized feedback");
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));

        assertThrows(UnauthorizedException.class, () -> {
            feedbackService.submitFeedback(100L, request, citizen2Principal);
        });

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException if complaint is not resolved or closed")
    void submitFeedback_FailsIfNotResolvedOrClosed() {
        SubmitFeedbackRequest request = new SubmitFeedbackRequest(4, "Too early");
        when(complaintRepository.findById(200L)).thenReturn(Optional.of(inProgressComplaint));

        assertThrows(BadRequestException.class, () -> {
            feedbackService.submitFeedback(200L, request, citizen1Principal);
        });

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException if feedback already exists")
    void submitFeedback_FailsIfDuplicate() {
        SubmitFeedbackRequest request = new SubmitFeedbackRequest(4, "Second time");
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(feedbackRepository.existsByComplaintId(100L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            feedbackService.submitFeedback(100L, request, citizen1Principal);
        });

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException if rating is outside 1-5")
    void submitFeedback_FailsIfInvalidRating() {
        SubmitFeedbackRequest requestZero = new SubmitFeedbackRequest(0, "Invalid 0");
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));

        assertThrows(BadRequestException.class, () -> {
            feedbackService.submitFeedback(100L, requestZero, citizen1Principal);
        });

        SubmitFeedbackRequest requestSix = new SubmitFeedbackRequest(6, "Invalid 6");
        assertThrows(BadRequestException.class, () -> {
            feedbackService.submitFeedback(100L, requestSix, citizen1Principal);
        });
    }

    @Test
    @DisplayName("Should retrieve feedback by complaint id for complaint owner")
    void getFeedbackByComplaintId_Success() {
        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .id(1L)
                .complaint(resolvedComplaint)
                .citizen(citizen1)
                .department(roadDept)
                .rating(5)
                .comments("Very happy")
                .createdAt(LocalDateTime.now())
                .build();

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(feedbackRepository.findByComplaintId(100L)).thenReturn(Optional.of(feedback));

        FeedbackResponse response = feedbackService.getFeedbackByComplaintId(100L, citizen1Principal);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Very happy", response.getComments());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no feedback exists for complaint")
    void getFeedbackByComplaintId_NotFound() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(feedbackRepository.findByComplaintId(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            feedbackService.getFeedbackByComplaintId(100L, citizen1Principal);
        });
    }

    @Test
    @DisplayName("Should calculate satisfaction stats aggregated across departments")
    void getFeedbackStats_Success() {
        when(feedbackRepository.getAverageSatisfactionRating()).thenReturn(4.5);
        when(feedbackRepository.count()).thenReturn(10L);
        when(feedbackRepository.countByRating(1)).thenReturn(0L);
        when(feedbackRepository.countByRating(2)).thenReturn(1L);
        when(feedbackRepository.countByRating(3)).thenReturn(1L);
        when(feedbackRepository.countByRating(4)).thenReturn(3L);
        when(feedbackRepository.countByRating(5)).thenReturn(5L);

        when(departmentRepository.findAll()).thenReturn(List.of(roadDept));
        when(feedbackRepository.getAverageSatisfactionRatingByDepartmentId(10L)).thenReturn(4.5);
        when(feedbackRepository.countByDepartmentId(10L)).thenReturn(10L);

        FeedbackStatsResponse stats = feedbackService.getFeedbackStats();

        assertNotNull(stats);
        assertEquals(4.5, stats.getOverallAverageRating());
        assertEquals(10L, stats.getTotalReviews());
        assertEquals(5L, stats.getRatingDistribution().get(5));
        assertEquals(1, stats.getDepartmentStats().size());
        assertEquals("ROAD_WORKS", stats.getDepartmentStats().get(0).getDepartmentCode());
    }
}
