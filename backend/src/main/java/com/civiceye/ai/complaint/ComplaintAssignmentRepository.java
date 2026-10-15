package com.civiceye.ai.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, Long> {

    List<ComplaintAssignment> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);

    List<ComplaintAssignment> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    Optional<ComplaintAssignment> findByComplaintIdAndIsActiveTrue(Long complaintId);

    List<ComplaintAssignment> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<ComplaintAssignment> findByAssignedToIdAndIsActiveTrue(Long assignedToId);

    long countByDepartmentIdAndIsActiveTrue(Long departmentId);
}
