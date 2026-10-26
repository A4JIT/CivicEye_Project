package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintFilterRequest;
import com.civiceye.ai.complaint.dto.ComplaintMonitoringSummaryDto;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ComplaintMonitoringService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;

    public ComplaintMonitoringService(ComplaintRepository complaintRepository,
                                      DepartmentRepository departmentRepository) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> filterComplaints(ComplaintFilterRequest request) {
        int page = request != null && request.getPage() != null ? Math.max(0, request.getPage()) : 0;
        int size = request != null && request.getSize() != null ? Math.max(1, request.getSize()) : 20;
        String sortBy = request != null && request.getSortBy() != null && !request.getSortBy().isBlank()
                ? request.getSortBy()
                : "createdAt";
        Sort.Direction direction = request != null && "ASC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Specification<Complaint> spec = buildMonitoringSpecification(request);

        return complaintRepository.findAll(spec, pageable).map(ComplaintResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ComplaintMonitoringSummaryDto getMonitoringSummary() {
        long total = complaintRepository.count();

        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW);
        long needsInfo = complaintRepository.countByStatus(ComplaintStatus.NEEDS_INFORMATION);
        long assigned = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        // Status breakdown
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (ComplaintStatus cs : ComplaintStatus.values()) {
            statusBreakdown.put(cs.name(), complaintRepository.countByStatus(cs));
        }

        // Category breakdown
        Map<String, Long> categoryBreakdown = new LinkedHashMap<>();
        for (ComplaintCategory cat : ComplaintCategory.values()) {
            categoryBreakdown.put(cat.name(), complaintRepository.countByCategory(cat));
        }

        // Department breakdown
        Map<String, Long> departmentBreakdown = new LinkedHashMap<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            departmentBreakdown.put(dept.getName(), complaintRepository.countByDepartmentId(dept.getId()));
        }

        // Priority breakdown
        Map<String, Long> priorityBreakdown = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            Specification<Complaint> pSpec = (root, query, cb) -> cb.equal(root.get("priority"), p);
            priorityBreakdown.put(p.name(), complaintRepository.count(pSpec));
        }

        // Critical Pending count (CRITICAL complaints not in RESOLVED, CLOSED, or REJECTED)
        Specification<Complaint> criticalSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("priority"), Priority.CRITICAL),
                cb.not(root.get("status").in(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, ComplaintStatus.REJECTED))
        );
        long criticalPendingCount = complaintRepository.count(criticalSpec);

        // Resolution rate (Resolved + Closed / total active complaints)
        double resolutionRate = 0.0;
        long totalResolvedAndClosed = resolved + closed;
        if (total > 0) {
            resolutionRate = Math.round(((double) totalResolvedAndClosed / (double) total) * 1000.0) / 10.0;
        }

        return ComplaintMonitoringSummaryDto.builder()
                .totalComplaints(total)
                .pendingReviewCount(submitted)
                .underReviewCount(underReview)
                .needsInfoCount(needsInfo)
                .assignedCount(assigned)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .closedCount(closed)
                .rejectedCount(rejected)
                .criticalPendingCount(criticalPendingCount)
                .resolutionRatePercentage(resolutionRate)
                .statusBreakdown(statusBreakdown)
                .departmentBreakdown(departmentBreakdown)
                .categoryBreakdown(categoryBreakdown)
                .priorityBreakdown(priorityBreakdown)
                .build();
    }

    private Specification<Complaint> buildMonitoringSpecification(ComplaintFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Status filter
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Department filter
            if (filter.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), filter.getDepartmentId()));
            }

            // Category filter
            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            // Priority filter
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }

            // Date Range filter
            if (filter.getStartDateTime() != null && filter.getEndDateTime() != null) {
                predicates.add(cb.between(root.get("createdAt"), filter.getStartDateTime(), filter.getEndDateTime()));
            } else if (filter.getStartDateTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDateTime()));
            } else if (filter.getEndDateTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDateTime()));
            }

            // Keyword Search (matching title, description, complaintNumber, or location)
            if (filter.getSearch() != null && !filter.getSearch().trim().isBlank()) {
                String term = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("complaintNumber")), term),
                        cb.like(cb.lower(root.get("title")), term),
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(root.get("location")), term)
                );
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
