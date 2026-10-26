package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.AssignComplaintRequest;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.StatusUpdateRequest;
import com.civiceye.ai.complaint.dto.TimelineEventResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.storage.FileStorageService;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintLifecycleServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ComplaintFeedbackRepository feedbackRepository;

    @InjectMocks
    private ComplaintLifecycleService lifecycleService;

    private User sampleCitizen;
    private User sampleOfficer;
    private User sampleAdmin;
    private Department sampleDept;
    private Complaint sampleComplaint;
    private UserPrincipal adminPrincipal;
    private UserPrincipal officerPrincipal;
    private UserPrincipal citizenPrincipal;

    @BeforeEach
    void setUp() {
        sampleCitizen = User.builder()
                .id(1L)
                .username("citizen_jane")
                .fullName("Jane Citizen")
                .email("citizen@civiceye.com")
                .role(Role.CITIZEN)
                .build();

        sampleOfficer = User.builder()
                .id(2L)
                .username("officer_sharma")
                .fullName("Rajesh Sharma")
                .email("officer@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .build();

        sampleAdmin = User.builder()
                .id(3L)
                .username("admin")
                .fullName("City Admin")
                .email("admin@civiceye.com")
                .role(Role.ADMIN)
                .build();

        sampleDept = Department.builder()
                .id(10L)
                .name("Roads & Traffic")
                .code("ROADS")
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Hazardous Pothole")
                .description("Deep hole in middle lane")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .location("MG Road")
                .citizen(sampleCitizen)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        adminPrincipal = UserPrincipal.create(sampleAdmin);
        officerPrincipal = UserPrincipal.create(sampleOfficer);
        citizenPrincipal = UserPrincipal.create(sampleCitizen);
    }

    @Test
    @DisplayName("Assign complaint transitions status to ASSIGNED and records audit history")
    void assignComplaint_Success() {
        AssignComplaintRequest request = new AssignComplaintRequest(10L, 2L, "Inspect road immediately");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(sampleDept));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOfficer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(sampleAdmin));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.assignComplaint(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.ASSIGNED, response.getStatus());
        assertEquals(10L, response.getDepartmentId());
        assertEquals(2L, response.getAssignedToId());
        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Update status advances workflow and saves resolution details")
    void updateStatus_ToResolved_Success() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleComplaint.setDepartment(sampleDept);
        sampleComplaint.setAssignedTo(sampleOfficer);

        StatusUpdateRequest request = new StatusUpdateRequest(ComplaintStatus.RESOLVED, "Pothole filled and sealed with asphalt");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOfficer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.updateStatus(100L, request, null, officerPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.RESOLVED, response.getStatus());
        assertEquals("Pothole filled and sealed with asphalt", response.getResolutionNotes());
        assertNotNull(response.getResolvedAt());
        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Update status with resolution photo stores image and records url")
    void updateStatus_WithResolutionImage_Success() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleComplaint.setDepartment(sampleDept);

        MockMultipartFile file = new MockMultipartFile("image", "fixed.jpg", "image/jpeg", "image_data".getBytes());
        StatusUpdateRequest request = new StatusUpdateRequest(ComplaintStatus.RESOLVED, "Repaired successfully");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(3L)).thenReturn(Optional.of(sampleAdmin));
        when(fileStorageService.storeFile(file)).thenReturn("uuid-fixed.jpg");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.updateStatus(100L, request, file, adminPrincipal);

        assertNotNull(response);
        assertEquals("/api/files/uuid-fixed.jpg", response.getResolutionImageUrl());
        verify(fileStorageService, times(1)).storeFile(file);
    }

    @Test
    @DisplayName("Citizen cannot update status and receives UnauthorizedException")
    void updateStatus_ByCitizen_Forbidden() {
        StatusUpdateRequest request = new StatusUpdateRequest(ComplaintStatus.IN_PROGRESS, "Try update");

        assertThrows(UnauthorizedException.class, () ->
                lifecycleService.updateStatus(100L, request, null, citizenPrincipal)
        );
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Illegal transition throws BadRequestException")
    void updateStatus_IllegalTransition_ThrowsBadRequest() {
        // Trying to jump from SUBMITTED directly to RESOLVED without assignment or progress
        sampleComplaint.setStatus(ComplaintStatus.SUBMITTED);
        sampleComplaint.setDepartment(sampleDept);

        StatusUpdateRequest request = new StatusUpdateRequest(ComplaintStatus.RESOLVED, "Illegal jump");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () ->
                lifecycleService.updateStatus(100L, request, null, officerPrincipal)
        );
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Same status update throws BadRequestException")
    void updateStatus_SameStatus_ThrowsBadRequest() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleComplaint.setDepartment(sampleDept);

        StatusUpdateRequest request = new StatusUpdateRequest(ComplaintStatus.IN_PROGRESS, "No change");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () ->
                lifecycleService.updateStatus(100L, request, null, officerPrincipal)
        );
    }

    @Test
    @DisplayName("Get timeline returns chronological audit trail for authorized citizen")
    void getTimeline_Success() {
        ComplaintStatusHistory history1 = ComplaintStatusHistory.builder()
                .id(1L)
                .complaint(sampleComplaint)
                .fromStatus(null)
                .toStatus(ComplaintStatus.SUBMITTED)
                .changedBy(sampleCitizen)
                .remarks("Complaint registered")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        ComplaintStatusHistory history2 = ComplaintStatusHistory.builder()
                .id(2L)
                .complaint(sampleComplaint)
                .fromStatus(ComplaintStatus.SUBMITTED)
                .toStatus(ComplaintStatus.ASSIGNED)
                .changedBy(sampleAdmin)
                .remarks("Assigned to Roads")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of(history1, history2));

        List<TimelineEventResponse> timeline = lifecycleService.getTimeline(100L, citizenPrincipal);

        assertEquals(2, timeline.size());
        assertEquals(ComplaintStatus.SUBMITTED, timeline.get(0).getToStatus());
        assertEquals(ComplaintStatus.ASSIGNED, timeline.get(1).getToStatus());
    }

    @Test
    @DisplayName("Citizen cannot view timeline of another citizen's complaint")
    void getTimeline_OtherCitizen_Forbidden() {
        User otherCitizen = User.builder().id(999L).build();
        sampleComplaint.setCitizen(otherCitizen);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(UnauthorizedException.class, () ->
                lifecycleService.getTimeline(100L, citizenPrincipal)
        );
    }

    @Test
    @DisplayName("Get complaints by department returns department complaints")
    void getComplaintsByDepartment_Success() {
        sampleComplaint.setDepartment(sampleDept);
        when(complaintRepository.findByDepartmentIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(sampleComplaint));

        List<ComplaintResponse> list = lifecycleService.getComplaintsByDepartment(10L, officerPrincipal);

        assertEquals(1, list.size());
        assertEquals("CE-2026-0001", list.get(0).getComplaintNumber());
    }
}
