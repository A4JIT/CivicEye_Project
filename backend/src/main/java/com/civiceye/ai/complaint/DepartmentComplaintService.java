package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.storage.FileStorageService;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DepartmentComplaintService(ComplaintRepository complaintRepository,
                                      ComplaintStatusHistoryRepository statusHistoryRepository,
                                      UserRepository userRepository,
                                      FileStorageService fileStorageService) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> searchDepartmentComplaints(List<ComplaintStatus> statuses,
                                                              Priority priority,
                                                              Long departmentId,
                                                              String search,
                                                              Pageable pageable,
                                                              UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        Long effectiveDeptId;
        if (currentUser.getRole() == Role.ADMIN) {
            effectiveDeptId = departmentId;
        } else if (currentUser.getRole() == Role.DEPARTMENT_USER) {
            User officer = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

            if (officer.getDepartment() == null) {
                throw new UnauthorizedException("Department officer is not assigned to any municipal department");
            }

            if (departmentId != null && !departmentId.equals(officer.getDepartment().getId())) {
                throw new UnauthorizedException("Department officers can only view complaints within their own department");
            }
            effectiveDeptId = officer.getDepartment().getId();
        } else {
            throw new UnauthorizedException("Access denied: Department officer or administrator privileges required");
        }

        Specification<Complaint> spec = Specification.where(ComplaintSpecifications.withDepartment(effectiveDeptId))
                .and(ComplaintSpecifications.withStatuses(statuses))
                .and(ComplaintSpecifications.withPriority(priority))
                .and(ComplaintSpecifications.withSearch(search));

        return complaintRepository.findAll(spec, pageable).map(ComplaintResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public DepartmentComplaintDetailResponse getDepartmentComplaintById(Long complaintId, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        verifyDepartmentBoundary(complaint, currentUser);

        List<TimelineEventResponse> timeline = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId)
                .stream()
                .map(TimelineEventResponse::fromEntity)
                .collect(Collectors.toList());

        return DepartmentComplaintDetailResponse.fromEntity(complaint, timeline);
    }

    @Transactional
    public ComplaintResponse startWork(Long complaintId, StartWorkRequest request, UserPrincipal currentUser) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        User actingUser = verifyDepartmentBoundary(complaint, currentUser);

        if (complaint.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new BadRequestException("Cannot start work on a complaint with status: " + complaint.getStatus()
                    + ". Only complaints in ASSIGNED status can be started.");
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        if (complaint.getStartedWorkingAt() == null) {
            complaint.setStartedWorkingAt(LocalDateTime.now());
        }

        String remarks = (request != null && request.getRemarks() != null && !request.getRemarks().isBlank())
                ? request.getRemarks().trim()
                : "Department maintenance crew initiated field repair work";

        complaint.setProgressNotes(remarks);

        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.IN_PROGRESS)
                .changedBy(actingUser)
                .remarks(remarks)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse addProgressNote(Long complaintId, ProgressNoteRequest request, UserPrincipal currentUser) {
        if (request == null || request.getNotes() == null || request.getNotes().isBlank()) {
            throw new BadRequestException("Progress note cannot be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        User actingUser = verifyDepartmentBoundary(complaint, currentUser);

        if (complaint.getStatus() == ComplaintStatus.RESOLVED
                || complaint.getStatus() == ComplaintStatus.CLOSED
                || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new BadRequestException("Cannot add progress notes to complaint in status: " + complaint.getStatus());
        }

        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS && complaint.getStatus() != ComplaintStatus.ASSIGNED) {
            throw new BadRequestException("Cannot add progress notes to complaint in status: " + complaint.getStatus());
        }

        String newNote = request.getNotes().trim();
        complaint.setProgressNotes(newNote);

        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(savedComplaint.getStatus())
                .toStatus(savedComplaint.getStatus())
                .changedBy(actingUser)
                .remarks("Progress update: " + newNote)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Transactional
    public ComplaintResponse resolveComplaint(Long complaintId,
                                              ResolveComplaintRequest request,
                                              MultipartFile resolutionImage,
                                              UserPrincipal currentUser) {
        if (request == null || request.getResolutionNotes() == null || request.getResolutionNotes().isBlank()) {
            throw new BadRequestException("Resolution notes are mandatory and cannot be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        User actingUser = verifyDepartmentBoundary(complaint, currentUser);

        if (complaint.getStatus() == ComplaintStatus.RESOLVED
                || complaint.getStatus() == ComplaintStatus.CLOSED
                || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new BadRequestException("Cannot resolve a complaint that is already " + complaint.getStatus());
        }

        if (complaint.getStatus() == ComplaintStatus.SUBMITTED
                || complaint.getStatus() == ComplaintStatus.UNDER_REVIEW
                || complaint.getStatus() == ComplaintStatus.NEEDS_INFORMATION) {
            throw new BadRequestException("Cannot resolve complaint in status " + complaint.getStatus()
                    + "; complaint must be assigned or in progress before resolution.");
        }

        String finalResolutionImageUrl = request.getResolutionImageUrl();
        if (resolutionImage != null && !resolutionImage.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(resolutionImage);
            finalResolutionImageUrl = "/api/files/" + storedFileName;
        }

        ComplaintStatus oldStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setResolutionNotes(request.getResolutionNotes().trim());
        if (finalResolutionImageUrl != null) {
            complaint.setResolutionImageUrl(finalResolutionImageUrl);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                .complaint(savedComplaint)
                .fromStatus(oldStatus)
                .toStatus(ComplaintStatus.RESOLVED)
                .changedBy(actingUser)
                .remarks(request.getResolutionNotes().trim())
                .resolutionImageUrl(finalResolutionImageUrl)
                .createdAt(LocalDateTime.now())
                .build();
        statusHistoryRepository.save(history);

        return ComplaintResponse.fromEntity(savedComplaint);
    }

    private User verifyDepartmentBoundary(Complaint complaint, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User session is unauthenticated");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        if (currentUser.getRole() == Role.ADMIN) {
            return user;
        }

        if (currentUser.getRole() != Role.DEPARTMENT_USER) {
            throw new UnauthorizedException("Access denied: Department officer or administrator privileges required");
        }

        if (user.getDepartment() == null) {
            throw new UnauthorizedException("Department officer is not assigned to any municipal department");
        }

        if (complaint.getDepartment() == null) {
            throw new UnauthorizedException("Complaint is not assigned to any department");
        }

        if (!user.getDepartment().getId().equals(complaint.getDepartment().getId())) {
            throw new UnauthorizedException("Department officers can only update complaints within their own department");
        }

        return user;
    }
}
