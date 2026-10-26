package com.civiceye.ai.complaint;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ComplaintSpecifications {

    public static Specification<Complaint> withStatuses(List<ComplaintStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Complaint> withDepartment(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    public static Specification<Complaint> withPriority(Priority priority) {
        return (root, query, cb) -> {
            if (priority == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("priority"), priority);
        };
    }

    public static Specification<Complaint> withCategory(ComplaintCategory category) {
        return (root, query, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    public static Specification<Complaint> withSearch(String queryStr) {
        return (root, query, cb) -> {
            if (queryStr == null || queryStr.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + queryStr.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("complaintNumber")), pattern),
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern)
            );
        };
    }
}
