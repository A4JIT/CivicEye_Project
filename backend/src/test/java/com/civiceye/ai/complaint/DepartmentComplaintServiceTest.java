package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.*;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DepartmentComplaintService service;

    private Department roadsDept;
    private Department waterDept;
    private User roadsOfficer;
    private User waterOfficer;
    private User adminUser;
    private User citizenUser;
    private User unassignedOfficer;
    private UserPrincipal roadsOfficerPrincipal;
    private UserPrincipal waterOfficerPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal citizenPrincipal;
    private UserPrincipal unassignedOfficerPrincipal;
    private Complaint sampleComplaint;

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

        roadsOfficer = User.builder()
                .id(20L)
                .username("officer_sharma")
                .fullName("Rajesh Sharma")
                .email("dept_roads@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(roadsDept)
                .build();

        waterOfficer = User.builder()
                .id(30L)
                .username("officer_verma")
                .fullName("Sunil Verma")
                .email("dept_water@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(waterDept)
                .build();

        unassignedOfficer = User.builder()
                .id(40L)
                .username("officer_unassigned")
                .fullName("Unassigned Officer")
                .email("dept_none@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(null)
                .build();

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .fullName("CivicEye Admin")
                .email("admin@civiceye.com")
                .role(Role.ADMIN)
                .build();

        citizenUser = User.builder()
                .id(10L)
                .username("citizen_jane")
                .fullName("Jane Citizen")
                .email("citizen@civiceye.com")
                .role(Role.CITIZEN)
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Damaged asphalt road")
                .description("Deep crater near traffic signal")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.ASSIGNED)
                .priority(Priority.HIGH)
                .location("MG Road Junction")
                .citizen(citizenUser)
                .department(roadsDept)
                .assignedTo(roadsOfficer)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        roadsOfficerPrincipal = UserPrincipal.create(roadsOfficer);
        waterOfficerPrincipal = UserPrincipal.create(waterOfficer);
        unassignedOfficerPrincipal = UserPrincipal.create(unassignedOfficer);
        adminPrincipal = UserPrincipal.create(adminUser);
        citizenPrincipal = UserPrincipal.create(citizenUser);
    }

    @Test
    @DisplayName("searchDepartmentComplaints: scopes strictly to officer's department")
    void searchDepartmentComplaints_ScopesToOfficerDepartment() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Complaint> page = new PageImpl<>(List.of(sampleComplaint));

        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(complaintRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ComplaintResponse> result = service.searchDepartmentComplaints(
                List.of(ComplaintStatus.ASSIGNED),
                Priority.HIGH,
                null,
                "crater",
                pageable,
                roadsOfficerPrincipal
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CE-2026-0001", result.getContent().get(0).getComplaintNumber());
    }

    @Test
    @DisplayName("searchDepartmentComplaints: throws Unauthorized if officer has no assigned department")
    void searchDepartmentComplaints_OfficerWithoutDepartment_ThrowsUnauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(40L)).thenReturn(Optional.of(unassignedOfficer));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                service.searchDepartmentComplaints(null, null, null, null, pageable, unassignedOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Department officer is not assigned to any municipal department"));
    }

    @Test
    @DisplayName("searchDepartmentComplaints: throws Unauthorized if officer requests a different department")
    void searchDepartmentComplaints_CrossDepartmentRequest_ThrowsUnauthorized() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                service.searchDepartmentComplaints(null, null, 2L, null, pageable, roadsOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Department officers can only view complaints within their own department"));
    }

    @Test
    @DisplayName("searchDepartmentComplaints: admin can search without restriction")
    void searchDepartmentComplaints_Admin_AllowsSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Complaint> page = new PageImpl<>(List.of(sampleComplaint));

        when(complaintRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ComplaintResponse> result = service.searchDepartmentComplaints(
                null, null, 1L, null, pageable, adminPrincipal
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("getDepartmentComplaintById: succeeds when complaint belongs to officer's department")
    void getDepartmentComplaintById_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(Collections.emptyList());

        DepartmentComplaintDetailResponse response = service.getDepartmentComplaintById(100L, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals("CE-2026-0001", response.getComplaintNumber());
        assertEquals("Public Works & Roads", response.getDepartmentName());
        assertNotNull(response.getTimeline());
    }

    @Test
    @DisplayName("getDepartmentComplaintById: cross-department officer throws Unauthorized")
    void getDepartmentComplaintById_CrossDepartment_ThrowsUnauthorized() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(30L)).thenReturn(Optional.of(waterOfficer));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                service.getDepartmentComplaintById(100L, waterOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Department officers can only update complaints within their own department"));
    }

    @Test
    @DisplayName("startWork: successfully transitions ASSIGNED -> IN_PROGRESS and sets timestamp")
    void startWork_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        StartWorkRequest request = new StartWorkRequest("Mobilized road crew with asphalt repair equipment");
        ComplaintResponse response = service.startWork(100L, request, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.IN_PROGRESS, response.getStatus());
        assertEquals("Mobilized road crew with asphalt repair equipment", response.getProgressNotes());
        assertNotNull(sampleComplaint.getStartedWorkingAt());

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
        verify(complaintRepository, times(1)).save(sampleComplaint);
    }

    @Test
    @DisplayName("startWork: throws BadRequest if complaint is not in ASSIGNED status")
    void startWork_NotAssigned_ThrowsBadRequest() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.startWork(100L, new StartWorkRequest("Already started"), roadsOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Only complaints in ASSIGNED status can be started"));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("addProgressNote: successfully records progress note and timeline entry")
    void addProgressNote_Success() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ProgressNoteRequest request = new ProgressNoteRequest("Sub-base compacted; asphalt overlay underway");
        ComplaintResponse response = service.addProgressNote(100L, request, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals("Sub-base compacted; asphalt overlay underway", response.getProgressNotes());
        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("addProgressNote: blank note throws BadRequest")
    void addProgressNote_BlankNote_ThrowsBadRequest() {
        ProgressNoteRequest request = new ProgressNoteRequest("   ");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.addProgressNote(100L, request, roadsOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Progress note cannot be empty"));
    }

    @Test
    @DisplayName("addProgressNote: on RESOLVED complaint throws BadRequest")
    void addProgressNote_OnResolved_ThrowsBadRequest() {
        sampleComplaint.setStatus(ComplaintStatus.RESOLVED);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));

        assertThrows(BadRequestException.class, () ->
                service.addProgressNote(100L, new ProgressNoteRequest("Late note"), roadsOfficerPrincipal)
        );
    }

    @Test
    @DisplayName("resolveComplaint: successfully marks RESOLVED with notes, photo and timestamp")
    void resolveComplaint_Success() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        MockMultipartFile proofFile = new MockMultipartFile(
                "image",
                "repaired.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );

        ResolveComplaintRequest request = new ResolveComplaintRequest(
                "Pothole filled with asphalt mastic and leveled flush with road surface."
        );

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));
        when(fileStorageService.storeFile(proofFile)).thenReturn("resolved-uuid-100.jpg");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintResponse response = service.resolveComplaint(100L, request, proofFile, roadsOfficerPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.RESOLVED, response.getStatus());
        assertEquals("Pothole filled with asphalt mastic and leveled flush with road surface.", response.getResolutionNotes());
        assertEquals("/api/files/resolved-uuid-100.jpg", response.getResolutionImageUrl());
        assertNotNull(response.getResolvedAt());

        verify(fileStorageService, times(1)).storeFile(proofFile);
        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("resolveComplaint: blank notes throws BadRequest")
    void resolveComplaint_BlankNotes_ThrowsBadRequest() {
        ResolveComplaintRequest request = new ResolveComplaintRequest("   ");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                service.resolveComplaint(100L, request, null, roadsOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Resolution notes are mandatory"));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolveComplaint: unassigned/submitted complaint throws BadRequest")
    void resolveComplaint_SubmittedStatus_ThrowsBadRequest() {
        sampleComplaint.setStatus(ComplaintStatus.SUBMITTED);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(20L)).thenReturn(Optional.of(roadsOfficer));

        ResolveComplaintRequest request = new ResolveComplaintRequest("Premature resolution attempt");

        assertThrows(BadRequestException.class, () ->
                service.resolveComplaint(100L, request, null, roadsOfficerPrincipal)
        );
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolveComplaint: cross-department officer throws Unauthorized")
    void resolveComplaint_CrossDepartmentOfficer_ThrowsUnauthorized() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(30L)).thenReturn(Optional.of(waterOfficer));

        ResolveComplaintRequest request = new ResolveComplaintRequest("Cross dept resolution");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                service.resolveComplaint(100L, request, null, waterOfficerPrincipal)
        );
        assertTrue(ex.getMessage().contains("Department officers can only update complaints within their own department"));
        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolveComplaint: Admin can resolve complaints across departments")
    void resolveComplaint_Admin_SucceedsAcrossDepartments() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);

        ResolveComplaintRequest request = new ResolveComplaintRequest("Administrative resolution override");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintResponse response = service.resolveComplaint(100L, request, null, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.RESOLVED, response.getStatus());
        assertEquals("Administrative resolution override", response.getResolutionNotes());
    }
}
