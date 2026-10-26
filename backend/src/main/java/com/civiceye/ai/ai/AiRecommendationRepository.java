package com.civiceye.ai.ai;

import com.civiceye.ai.complaint.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {

    Optional<AiRecommendation> findByComplaintId(Long complaintId);

    Optional<AiRecommendation> findByComplaint(Complaint complaint);

    boolean existsByComplaintId(Long complaintId);

    List<AiRecommendation> findByIsOverridden(Boolean isOverridden);

    List<AiRecommendation> findBySuggestedDepartmentId(Long departmentId);

    List<AiRecommendation> findBySuggestedCategory(String suggestedCategory);
}
