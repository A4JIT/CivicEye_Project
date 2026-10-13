package com.civiceye.ai.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, Long> {

    Optional<ComplaintFeedback> findByComplaintId(Long complaintId);

    boolean existsByComplaintId(Long complaintId);

    List<ComplaintFeedback> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    @Query("SELECT AVG(f.rating) FROM ComplaintFeedback f")
    Double getAverageSatisfactionRating();

    long countByRating(Integer rating);
}
