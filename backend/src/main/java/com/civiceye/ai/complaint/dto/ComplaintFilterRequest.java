package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintCategory;
import com.civiceye.ai.complaint.ComplaintStatus;
import com.civiceye.ai.complaint.Priority;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ComplaintFilterRequest {

    private ComplaintStatus status;
    private Long departmentId;
    private ComplaintCategory category;
    private Priority priority;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private String search;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "DESC";

    public ComplaintFilterRequest() {
    }

    public ComplaintFilterRequest(ComplaintStatus status, Long departmentId, ComplaintCategory category,
                                  Priority priority, LocalDate startDate, LocalDate endDate,
                                  String search, Integer page, Integer size, String sortBy, String sortDir) {
        this.status = status;
        this.departmentId = departmentId;
        this.category = category;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.search = search;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
        this.sortBy = sortBy != null ? sortBy : "createdAt";
        this.sortDir = sortDir != null ? sortDir : "DESC";
    }

    public LocalDateTime getStartDateTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }

    public LocalDateTime getEndDateTime() {
        return endDate != null ? endDate.atTime(LocalTime.MAX) : null;
    }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public Integer getPage() { return page != null ? page : 0; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size != null ? size : 20; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy != null ? sortBy : "createdAt"; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir != null ? sortDir : "DESC"; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }

    public static ComplaintFilterRequestBuilder builder() {
        return new ComplaintFilterRequestBuilder();
    }

    public static class ComplaintFilterRequestBuilder {
        private ComplaintStatus status;
        private Long departmentId;
        private ComplaintCategory category;
        private Priority priority;
        private LocalDate startDate;
        private LocalDate endDate;
        private String search;
        private Integer page = 0;
        private Integer size = 20;
        private String sortBy = "createdAt";
        private String sortDir = "DESC";

        public ComplaintFilterRequestBuilder status(ComplaintStatus status) { this.status = status; return this; }
        public ComplaintFilterRequestBuilder departmentId(Long departmentId) { this.departmentId = departmentId; return this; }
        public ComplaintFilterRequestBuilder category(ComplaintCategory category) { this.category = category; return this; }
        public ComplaintFilterRequestBuilder priority(Priority priority) { this.priority = priority; return this; }
        public ComplaintFilterRequestBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public ComplaintFilterRequestBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public ComplaintFilterRequestBuilder search(String search) { this.search = search; return this; }
        public ComplaintFilterRequestBuilder page(Integer page) { this.page = page; return this; }
        public ComplaintFilterRequestBuilder size(Integer size) { this.size = size; return this; }
        public ComplaintFilterRequestBuilder sortBy(String sortBy) { this.sortBy = sortBy; return this; }
        public ComplaintFilterRequestBuilder sortDir(String sortDir) { this.sortDir = sortDir; return this; }

        public ComplaintFilterRequest build() {
            return new ComplaintFilterRequest(status, departmentId, category, priority, startDate, endDate, search, page, size, sortBy, sortDir);
        }
    }
}
