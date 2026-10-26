package com.civiceye.ai.complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<Complaint> findTop5ByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    long countByCitizenId(Long citizenId);

    long countByCitizenIdAndStatus(Long citizenId, ComplaintStatus status);

    List<Complaint> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    List<Complaint> findTop5ByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    Page<Complaint> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Complaint> findByDepartmentIdAndStatus(Long departmentId, ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByDepartmentIdAndStatusIn(Long departmentId, Collection<ComplaintStatus> statuses, Pageable pageable);

    Page<Complaint> findByAssignedToId(Long assignedToId, Pageable pageable);

    Page<Complaint> findByAssignedToIdAndStatus(Long assignedToId, ComplaintStatus status, Pageable pageable);

    long countByDepartmentId(Long departmentId);

    long countByDepartmentIdAndStatus(Long departmentId, ComplaintStatus status);

    long countByDepartmentIdAndStatusIn(Long departmentId, Collection<ComplaintStatus> statuses);

    long countByAssignedToIdAndStatus(Long assignedToId, ComplaintStatus status);

    long countByStatus(ComplaintStatus status);

    long countByCategory(ComplaintCategory category);

    List<Complaint> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.department.id = :deptId AND c.priority = 'CRITICAL' AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    long countCriticalPendingByDepartment(@Param("deptId") Long deptId);

    Optional<Complaint> findTopByComplaintNumberStartingWithOrderByIdDesc(String prefix);

    @Query("SELECT c FROM Complaint c WHERE c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED') AND c.category = :category")
    List<Complaint> findActiveByCategory(@Param("category") ComplaintCategory category);

    @Query("SELECT c FROM Complaint c WHERE c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    List<Complaint> findAllActive();
}
