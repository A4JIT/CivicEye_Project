package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.StatusUpdateRequest;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentComplaintLifecycleTest {

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

    @InjectMocks
    private ComplaintLifecycleService lifecycleService;

    private User sampleCitizen;
    private User roadsOfficer;
    private User waterOfficer;
    private Department roadsDept;
    private Department waterDept;
    private Complaint sampleComplaint;
    private UserPrincipal roadsOfficerPrincipal;
    private UserPrincipal waterOfficerPrincipal;

    @BeforeEach
    void setUp() {
        roadsDept = Department.builder()
                .id(1L)
                .name("Public Works & Roads")
                .code("ROAD_WORKS")
                .build();

        waterDept = Department.builder()
                .id(2L)
                .name("Water Supply & Sewerage")
                .code("WATER_SUPPLY")
                .build();

        sampleCitizen = User.builder()
                .id(10L)
                .username("citizen_jane")
                .fullName("Jane Citizen")
                .email("citizen@civiceye.com")
                .role(Role.CITIZEN)
                .build();

        roadsOfficer = User.builder()
                .id(20L)
                .username("officer_sharma")
                .fullName("Rajesh Sharma (PWD Officer)")
                .email("dept_roads@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(roadsDept)
                .build();

        waterOfficer = User.builder()
                .id(30L)
                .username("officer_verma")
                .fullName("Sunil Verma (Water Officer)")
                .email("dept_water@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(waterDept)
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Deep Crater Pothole")
                .description("Damaging vehicles near Metro Pillar 42")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.ASSIGNED)
                .priority(Priority.HIGH)
                .location("MG Road Junction")
                .citizen(sampleCitizen)
                .department(roadsDept)
                .assignedTo(roadsOfficer)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        roadsOfficerPrincipal = UserPrincipal.create(roadsOfficer);
        waterOfficerPrincipal = UserPrincipal.create(waterOfficer);
    }

    @Test
    @DisplayName("Department officer starts work: transitions ASSIGNED -> IN_PROGRESS and sets startedWorkingAt")
    void startWork_TransitionsToInProgress_SetsStartedWorkingAt() {
        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.IN_PROGRESS,
                "Field maintenance crew mobilized to site with road roller and cold asphalt patch"
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.updateStatus(100L, request, null, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.IN_PROGRESS, response.getStatus());
        assertEquals("Field maintenance crew mobilized to site with road roller and cold asphalt patch", response.getProgressNotes());
        assertNotNull(sampleComplaint.getStartedWorkingAt(), "startedWorkingAt timestamp must be stamped on start work");

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
        verify(complaintRepository, times(1)).save(sampleComplaint);
    }

    @Test
    @DisplayName("Department officer resolves complaint: transitions IN_PROGRESS -> RESOLVED and captures proof image")
    void resolveComplaint_CapturesResolutionNotes_TimestampAndImage() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleComplaint.setStartedWorkingAt(LocalDateTime.now().minusHours(4));

        MockMultipartFile proofFile = new MockMultipartFile(
                "resolutionImage",
                "resolved_road.jpg",
                "image/jpeg",
                "verified_repair_bytes".getBytes()
        );

        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.RESOLVED,
                "Cavity filled with bituminous aggregate, compacted and leveled flush with roadway."
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(fileStorageService.storeFile(proofFile)).thenReturn("res-proof-uuid-100.jpg");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.updateStatus(100L, request, proofFile, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.RESOLVED, response.getStatus());
        assertEquals("Cavity filled with bituminous aggregate, compacted and leveled flush with roadway.", response.getResolutionNotes());
        assertEquals("/api/files/res-proof-uuid-100.jpg", response.getResolutionImageUrl());
        assertNotNull(response.getResolvedAt(), "resolvedAt timestamp must be recorded");

        verify(fileStorageService, times(1)).storeFile(proofFile);
        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Department boundary enforcement: officer from another department receives UnauthorizedException")
    void departmentIsolation_CrossDepartmentOfficer_ThrowsUnauthorized() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.RESOLVED,
                "Unauthorized attempt to resolve cross-department issue"
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(30L)).thenReturn(Optional.of(waterOfficer));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                lifecycleService.updateStatus(100L, request, null, waterOfficerPrincipal)
        );

        assertTrue(ex.getMessage().contains("Department officers can only update complaints within their own department"));
        verify(complaintRepository, never()).save(any());
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unassigned complaint cannot be updated by department officer")
    void unassignedComplaint_DepartmentOfficerUpdate_ThrowsUnauthorized() {
        sampleComplaint.setDepartment(null);

        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.IN_PROGRESS,
                "Attempt to start work on unassigned complaint"
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                lifecycleService.updateStatus(100L, request, null, roadsOfficerPrincipal)
        );

        assertTrue(ex.getMessage().contains("Complaint is not assigned to any department"));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid state skip: department officer cannot jump from SUBMITTED directly to RESOLVED")
    void invalidTransition_SubmittedToResolved_ThrowsBadRequest() {
        sampleComplaint.setStatus(ComplaintStatus.SUBMITTED);

        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.RESOLVED,
                "Skip assignment jump"
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () ->
                lifecycleService.updateStatus(100L, request, null, roadsOfficerPrincipal)
        );

        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Status history records officer identifier and timestamp upon resolution")
    void statusHistory_RecordsAuditTrail_OnStatusChange() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        StatusUpdateRequest request = new StatusUpdateRequest(
                ComplaintStatus.RESOLVED,
                "Maintenance inspection confirmed complete."
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        lifecycleService.updateStatus(100L, request, null, roadsOfficerPrincipal);

        verify(statusHistoryRepository).save(argThat(history ->
                history.getComplaint().getId().equals(100L) &&
                history.getFromStatus() == ComplaintStatus.IN_PROGRESS &&
                history.getToStatus() == ComplaintStatus.RESOLVED &&
                history.getChangedBy().getId().equals(20L) &&
                history.getRemarks().equals("Maintenance inspection confirmed complete.") &&
                history.getCreatedAt() != null
        ));
    }
}
