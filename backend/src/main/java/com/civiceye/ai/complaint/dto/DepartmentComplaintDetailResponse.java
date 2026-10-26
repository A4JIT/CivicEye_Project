package com.civiceye.ai.complaint.dto;

import com.civiceye.ai.complaint.Complaint;

import java.util.ArrayList;
import java.util.List;

public class DepartmentComplaintDetailResponse extends ComplaintResponse {

    private List<TimelineEventResponse> timeline = new ArrayList<>();

    public DepartmentComplaintDetailResponse() {
        super();
    }

    public static DepartmentComplaintDetailResponse fromEntity(Complaint complaint, List<TimelineEventResponse> timeline) {
        if (complaint == null) return null;

        DepartmentComplaintDetailResponse res = new DepartmentComplaintDetailResponse();
        res.setId(complaint.getId());
        res.setComplaintNumber(complaint.getComplaintNumber());
        res.setTitle(complaint.getTitle());
        res.setDescription(complaint.getDescription());
        res.setCategory(complaint.getCategory());
        res.setStatus(complaint.getStatus());
        res.setPriority(complaint.getPriority());
        res.setLocation(complaint.getLocation());
        res.setLatitude(complaint.getLatitude());
        res.setLongitude(complaint.getLongitude());
        res.setImageUrl(complaint.getImageUrl());

        if (complaint.getCitizen() != null) {
            res.setCitizenId(complaint.getCitizen().getId());
            res.setCitizenName(complaint.getCitizen().getFullName());
            res.setCitizenEmail(complaint.getCitizen().getEmail());
        }

        if (complaint.getDepartment() != null) {
            res.setDepartmentId(complaint.getDepartment().getId());
            res.setDepartmentName(complaint.getDepartment().getName());
        }

        if (complaint.getAssignedTo() != null) {
            res.setAssignedToId(complaint.getAssignedTo().getId());
            res.setAssignedToName(complaint.getAssignedTo().getFullName());
        }

        res.setRejectionReason(complaint.getRejectionReason());
        res.setResolutionNotes(complaint.getResolutionNotes());
        res.setResolutionImageUrl(complaint.getResolutionImageUrl());
        res.setAdminNotes(complaint.getAdminNotes());
        res.setNeedsInfoRemarks(complaint.getNeedsInfoRemarks());
        res.setAiCategoryOverridden(complaint.getAiCategoryOverridden() != null ? complaint.getAiCategoryOverridden() : false);
        res.setAiPriorityOverridden(complaint.getAiPriorityOverridden() != null ? complaint.getAiPriorityOverridden() : false);
        res.setStartedWorkingAt(complaint.getStartedWorkingAt());
        res.setProgressNotes(complaint.getProgressNotes());
        res.setCreatedAt(complaint.getCreatedAt());
        res.setUpdatedAt(complaint.getUpdatedAt());
        res.setResolvedAt(complaint.getResolvedAt());

        res.setTimeline(timeline != null ? timeline : new ArrayList<>());
        return res;
    }

    public List<TimelineEventResponse> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEventResponse> timeline) {
        this.timeline = timeline;
    }
}
