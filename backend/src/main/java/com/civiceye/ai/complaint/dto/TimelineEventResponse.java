package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.ComplaintStatus;
import com.civiceye.ai.complaint.ComplaintStatusHistory;

import java.time.LocalDateTime;

public class TimelineEventResponse {

    private Long id;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private Long changedById;
    private String changedByName;
    private String changedByRole;
    private String remarks;
    private String resolutionImageUrl;
    private LocalDateTime createdAt;

    public TimelineEventResponse() {
    }

    public static TimelineEventResponse fromEntity(ComplaintStatusHistory history) {
        if (history == null) return null;
        TimelineEventResponse res = new TimelineEventResponse();
        res.setId(history.getId());
        res.setFromStatus(history.getFromStatus());
        res.setToStatus(history.getToStatus());
        if (history.getChangedBy() != null) {
            res.setChangedById(history.getChangedBy().getId());
            res.setChangedByName(history.getChangedBy().getFullName());
            res.setChangedByRole(history.getChangedBy().getRole() != null ? history.getChangedBy().getRole().name() : null);
        }
        res.setRemarks(history.getRemarks());
        res.setResolutionImageUrl(history.getResolutionImageUrl());
        res.setCreatedAt(history.getCreatedAt());
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public Long getChangedById() {
        return changedById;
    }

    public void setChangedById(Long changedById) {
        this.changedById = changedById;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getChangedByRole() {
        return changedByRole;
    }

    public void setChangedByRole(String changedByRole) {
        this.changedByRole = changedByRole;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getResolutionImageUrl() {
        return resolutionImageUrl;
    }

    public void setResolutionImageUrl(String resolutionImageUrl) {
        this.resolutionImageUrl = resolutionImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
