package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminComplaintServiceTest {

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
    private AdminComplaintService adminComplaintService;

    private User adminUserEntity;
    private User citizenUserEntity;
    private UserPrincipal adminPrincipal;
    private UserPrincipal citizenPrincipal;
    private Department sampleDepartment;
    private Complaint sampleComplaint;

    @BeforeEach
    void setUp() {
        adminUserEntity = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@civiceye.com")
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .build();

        citizenUserEntity = User.builder()
                .id(2L)
                .username("citizen_jane")
                .email("jane@example.com")
                .fullName("Jane Citizen")
                .role(Role.CITIZEN)
                .build();

        adminPrincipal = UserPrincipal.create(adminUserEntity);
        citizenPrincipal = UserPrincipal.create(citizenUserEntity);

        sampleDepartment = Department.builder()
                .id(10L)
                .name("Public Works & Roads")
                .code("PWD")
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Severe pothole on main avenue")
                .description("Dangerous road cavity causing traffic hazard")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .location("MG Road Junction")
                .citizen(citizenUserEntity)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
    }

    @Test
    @DisplayName("Admin can review complaint, override category/priority and advance status to UNDER_REVIEW")
    void testReviewComplaint_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUserEntity));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        AdminReviewRequest request = new AdminReviewRequest(
                "Verified severe pothole by photos",
                ComplaintCategory.ROADS_AND_FOOTPATHS,
                Priority.HIGH
        );

        ComplaintResponse response = adminComplaintService.reviewComplaint(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.UNDER_REVIEW, response.getStatus());
        assertEquals(Priority.HIGH, response.getPriority());
        assertTrue(response.getAiPriorityOverridden());
        assertEquals("Verified severe pothole by photos", response.getAdminNotes());

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Admin can reject complaint with mandatory reason")
    void testRejectComplaint_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUserEntity));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        RejectComplaintRequest request = new RejectComplaintRequest("Duplicate report of CE-2026-0000", "DUPLICATE");
        ComplaintResponse response = adminComplaintService.rejectComplaint(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.REJECTED, response.getStatus());
        assertTrue(response.getRejectionReason().contains("Duplicate report"));

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Rejecting complaint with blank reason throws BadRequestException")
    void testRejectComplaint_BlankReason_ThrowsException() {
        RejectComplaintRequest blankRequest = new RejectComplaintRequest("   ");

        assertThrows(BadRequestException.class, () ->
                adminComplaintService.rejectComplaint(100L, blankRequest, adminPrincipal));

        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin can request information, setting status to NEEDS_INFORMATION")
    void testRequestInformation_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUserEntity));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        RequestInfoRequest request = new RequestInfoRequest("Please provide landmark coordinates near street 4");
        ComplaintResponse response = adminComplaintService.requestInformation(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.NEEDS_INFORMATION, response.getStatus());
        assertEquals("Please provide landmark coordinates near street 4", response.getNeedsInfoRemarks());

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Citizen can respond to information request, moving status back to UNDER_REVIEW")
    void testProvideInformation_Success() {
        sampleComplaint.setStatus(ComplaintStatus.NEEDS_INFORMATION);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(userRepository.findById(2L)).thenReturn(Optional.of(citizenUserEntity));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ProvideInfoRequest request = new ProvideInfoRequest("Located right in front of Metro Pillar 14A");
        ComplaintResponse response = adminComplaintService.provideInformation(100L, request, null, citizenPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.UNDER_REVIEW, response.getStatus());

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Unauthorized citizen cannot respond to another citizen's complaint")
    void testProvideInformation_UnauthorizedCitizen() {
        sampleComplaint.setStatus(ComplaintStatus.NEEDS_INFORMATION);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        User otherCitizen = User.builder().id(99L).role(Role.CITIZEN).build();
        UserPrincipal otherPrincipal = UserPrincipal.create(otherCitizen);

        ProvideInfoRequest request = new ProvideInfoRequest("Some info");

        assertThrows(UnauthorizedException.class, () ->
                adminComplaintService.provideInformation(100L, request, null, otherPrincipal));
    }

    @Test
    @DisplayName("Admin can assign complaint to department with urgency adjustment")
    void testAssignDepartment_Success() {
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(sampleDepartment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUserEntity));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        AdminAssignRequest request = new AdminAssignRequest(10L, null, Priority.CRITICAL, "Urgent route dispatch");
        ComplaintResponse response = adminComplaintService.assignDepartment(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.ASSIGNED, response.getStatus());
        assertEquals(Priority.CRITICAL, response.getPriority());
        assertEquals("Public Works & Roads", response.getDepartmentName());

        verify(statusHistoryRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Non-admin user cannot invoke admin triage functions")
    void testNonAdmin_ThrowsUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () ->
                adminComplaintService.reviewComplaint(100L, new AdminReviewRequest(), citizenPrincipal));

        assertThrows(UnauthorizedException.class, () ->
                adminComplaintService.rejectComplaint(100L, new RejectComplaintRequest("Invalid"), citizenPrincipal));

        assertThrows(UnauthorizedException.class, () ->
                adminComplaintService.requestInformation(100L, new RequestInfoRequest("Info"), citizenPrincipal));

        assertThrows(UnauthorizedException.class, () ->
                adminComplaintService.assignDepartment(100L, new AdminAssignRequest(10L), citizenPrincipal));
    }

    @Test
    @DisplayName("Search complaints with specifications executes paginated query")
    @SuppressWarnings("unchecked")
    void testSearchComplaints_Success() {
        Page<Complaint> page = new PageImpl<>(List.of(sampleComplaint));
        when(complaintRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ComplaintResponse> result = adminComplaintService.searchComplaints(
                List.of(ComplaintStatus.SUBMITTED),
                10L,
                Priority.MEDIUM,
                ComplaintCategory.ROADS_AND_FOOTPATHS,
                "pothole",
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CE-2026-0001", result.getContent().get(0).getComplaintNumber());
    }
}
