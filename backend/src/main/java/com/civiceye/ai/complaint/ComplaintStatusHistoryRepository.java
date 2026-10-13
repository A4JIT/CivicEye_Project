package com.civiceye.ai.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {

    List<ComplaintStatusHistory> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);

    List<ComplaintStatusHistory> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    long countByComplaintId(Long complaintId);
}
