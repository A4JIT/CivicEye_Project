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
}
