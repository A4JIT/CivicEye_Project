package com.civiceye.ai.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, Long> {

    Optional<ComplaintFeedback> findByComplaintId(Long complaintId);

    boolean existsByComplaintId(Long complaintId);

    List<ComplaintFeedback> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<ComplaintFeedback> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    @Query("SELECT AVG(f.rating) FROM ComplaintFeedback f")
    Double getAverageSatisfactionRating();

    @Query("SELECT AVG(f.rating) FROM ComplaintFeedback f WHERE f.department.id = :departmentId")
    Double getAverageSatisfactionRatingByDepartmentId(@Param("departmentId") Long departmentId);

    long countByRating(Integer rating);

    long countByDepartmentId(Long departmentId);
}
