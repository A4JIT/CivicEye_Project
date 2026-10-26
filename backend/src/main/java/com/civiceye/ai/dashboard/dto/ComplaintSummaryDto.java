package com.civiceye.ai.dashboard.dto;

import com.civiceye.ai.complaint.Complaint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSummaryDto {

    private Long id;
    private String complaintNumber;
    private String title;
    private String category;
    private String status;
    private String priority;
    private String location;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getComplaintNumber() { return complaintNumber; }
    public void setComplaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ComplaintSummaryDto fromEntity(Complaint complaint) {
        if (complaint == null) return null;
        return ComplaintSummaryDto.builder()
                .id(complaint.getId())
                .complaintNumber(complaint.getComplaintNumber())
                .title(complaint.getTitle())
                .category(complaint.getCategory() != null ? complaint.getCategory().name() : null)
                .status(complaint.getStatus() != null ? complaint.getStatus().name() : null)
                .priority(complaint.getPriority() != null ? complaint.getPriority().name() : null)
                .location(complaint.getLocation())
                .departmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : "Unassigned")
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }

    public static ComplaintSummaryDtoBuilder builder() {
        return new ComplaintSummaryDtoBuilder();
    }

    public static class ComplaintSummaryDtoBuilder {
        private Long id;
        private String complaintNumber;
        private String title;
        private String category;
        private String status;
        private String priority;
        private String location;
        private String departmentName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ComplaintSummaryDtoBuilder id(Long id) { this.id = id; return this; }
        public ComplaintSummaryDtoBuilder complaintNumber(String complaintNumber) { this.complaintNumber = complaintNumber; return this; }
        public ComplaintSummaryDtoBuilder title(String title) { this.title = title; return this; }
        public ComplaintSummaryDtoBuilder category(String category) { this.category = category; return this; }
        public ComplaintSummaryDtoBuilder status(String status) { this.status = status; return this; }
        public ComplaintSummaryDtoBuilder priority(String priority) { this.priority = priority; return this; }
        public ComplaintSummaryDtoBuilder location(String location) { this.location = location; return this; }
        public ComplaintSummaryDtoBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public ComplaintSummaryDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ComplaintSummaryDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ComplaintSummaryDto build() {
            ComplaintSummaryDto dto = new ComplaintSummaryDto();
            dto.setId(this.id);
            dto.setComplaintNumber(this.complaintNumber);
            dto.setTitle(this.title);
            dto.setCategory(this.category);
            dto.setStatus(this.status);
            dto.setPriority(this.priority);
            dto.setLocation(this.location);
            dto.setDepartmentName(this.departmentName);
            dto.setCreatedAt(this.createdAt);
            dto.setUpdatedAt(this.updatedAt);
            return dto;
        }
    }
}
