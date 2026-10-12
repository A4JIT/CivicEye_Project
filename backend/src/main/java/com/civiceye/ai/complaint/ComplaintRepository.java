package com.civiceye.ai.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    List<Complaint> findTop5ByCitizenIdOrderByCreatedAtDesc(Long citizenId);

    long countByCitizenId(Long citizenId);

    long countByCitizenIdAndStatus(Long citizenId, ComplaintStatus status);

    List<Complaint> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    List<Complaint> findTop5ByDepartmentIdOrderByCreatedAtDesc(Long departmentId);

    long countByDepartmentId(Long departmentId);

    long countByDepartmentIdAndStatus(Long departmentId, ComplaintStatus status);

    long countByStatus(ComplaintStatus status);

    long countByCategory(ComplaintCategory category);

    List<Complaint> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.department.id = :deptId AND c.priority = 'CRITICAL' AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    long countCriticalPendingByDepartment(@Param("deptId") Long deptId);
}
