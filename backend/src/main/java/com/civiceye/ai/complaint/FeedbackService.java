package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.DepartmentFeedbackStats;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public FeedbackService(ComplaintRepository complaintRepository,
                           ComplaintFeedbackRepository feedbackRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository) {
        this.complaintRepository = complaintRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public FeedbackResponse submitFeedback(Long complaintId, SubmitFeedbackRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (currentUser.getRole() == Role.CITIZEN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Citizens can only submit feedback for their own complaints");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted for complaints in RESOLVED or CLOSED status");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5 stars");
        }

        if (feedbackRepository.existsByComplaintId(complaintId)) {
            throw new BadRequestException("Feedback has already been submitted for this complaint");
        }

        User citizen = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        ComplaintFeedback feedback = ComplaintFeedback.builder()
                .complaint(complaint)
                .citizen(citizen)
                .department(complaint.getDepartment())
                .rating(request.getRating())
                .comments(request.getComments() != null ? request.getComments().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        ComplaintFeedback saved = feedbackRepository.save(feedback);
        complaint.setFeedback(saved);
        complaintRepository.save(complaint);

        return FeedbackResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByComplaintId(Long complaintId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (currentUser.getRole() == Role.CITIZEN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Access denied: You can only view feedback for your own complaints");
        }

        ComplaintFeedback feedback = feedbackRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("No feedback found for complaint id: " + complaintId));

        return FeedbackResponse.fromEntity(feedback);
    }

    @Transactional(readOnly = true)
    public FeedbackStatsResponse getFeedbackStats() {
        Double avgRating = feedbackRepository.getAverageSatisfactionRating();
        long totalReviews = feedbackRepository.count();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int stars = 1; stars <= 5; stars++) {
            distribution.put(stars, feedbackRepository.countByRating(stars));
        }

        List<Department> departments = departmentRepository.findAll();
        List<DepartmentFeedbackStats> deptStats = departments.stream().map(dept -> {
            Double deptAvg = feedbackRepository.getAverageSatisfactionRatingByDepartmentId(dept.getId());
            long deptCount = feedbackRepository.countByDepartmentId(dept.getId());
            return DepartmentFeedbackStats.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getName())
                    .departmentCode(dept.getCode())
                    .averageRating(deptAvg != null ? Math.round(deptAvg * 100.0) / 100.0 : 0.0)
                    .totalReviews(deptCount)
                    .build();
        }).collect(Collectors.toList());

        return FeedbackStatsResponse.builder()
                .overallAverageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0)
                .totalReviews(totalReviews)
                .ratingDistribution(distribution)
                .departmentStats(deptStats)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByDepartment(Long departmentId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DEPARTMENT_USER) {
            throw new UnauthorizedException("Only department officers or administrators can view department feedback list");
        }

        return feedbackRepository.findByDepartmentIdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
