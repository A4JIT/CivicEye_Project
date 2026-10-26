package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.CloseComplaintRequest;
import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.ConfirmResolutionRequest;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.security.UserPrincipal;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintClosureServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComplaintFeedbackRepository feedbackRepository;

    @InjectMocks
    private ComplaintLifecycleService lifecycleService;

    private User citizen;
    private User otherCitizen;
    private User admin;
    private User officer;
    private Department roadDept;
    private Complaint resolvedComplaint;
    private Complaint inProgressComplaint;

    private UserPrincipal citizenPrincipal;
    private UserPrincipal otherCitizenPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal officerPrincipal;

    @BeforeEach
    void setUp() {
        citizen = User.builder()
                .id(1L)
                .username("citizen")
                .fullName("Citizen Sharma")
                .role(Role.CITIZEN)
                .build();

        otherCitizen = User.builder()
                .id(2L)
                .username("otherCitizen")
                .fullName("Other Citizen")
                .role(Role.CITIZEN)
                .build();

        admin = User.builder()
                .id(10L)
                .username("admin")
                .fullName("Administrator")
                .role(Role.ADMIN)
                .build();

        roadDept = Department.builder()
                .id(5L)
                .name("Roads & Infrastructure")
                .code("ROAD_WORKS")
                .build();

        officer = User.builder()
                .id(20L)
                .username("officer")
                .fullName("PWD Officer")
                .role(Role.DEPARTMENT_USER)
                .department(roadDept)
                .build();

        citizenPrincipal = UserPrincipal.create(citizen);
        otherCitizenPrincipal = UserPrincipal.create(otherCitizen);
        adminPrincipal = UserPrincipal.create(admin);
        officerPrincipal = UserPrincipal.create(officer);

        resolvedComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title("Road Pothole")
                .status(ComplaintStatus.RESOLVED)
                .citizen(citizen)
                .department(roadDept)
                .resolvedAt(LocalDateTime.now().minusHours(2))
                .resolutionNotes("Road resurfaced cleanly")
                .build();

        inProgressComplaint = Complaint.builder()
                .id(200L)
                .complaintNumber("CE-2026-0002")
                .title("Water Leak")
                .status(ComplaintStatus.IN_PROGRESS)
                .citizen(citizen)
                .department(roadDept)
                .build();
    }

    @Test
    @DisplayName("Citizen should confirm resolution and successfully transition complaint to CLOSED")
    void confirmResolution_Success() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Pothole fixed cleanly, verified.");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.confirmResolution(100L, request, citizenPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.CLOSED, response.getStatus());
        assertTrue(response.getCitizenConfirmedResolution());
        assertEquals("Pothole fixed cleanly, verified.", response.getClosureRemarks());
        assertNotNull(response.getClosedAt());

        verify(statusHistoryRepository).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Confirming resolution should fail if user is not the complaint owner")
    void confirmResolution_FailsIfNotOwner() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Attempt by other");
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));

        assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.confirmResolution(100L, request, otherCitizenPrincipal);
        });

        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Confirming resolution should fail if complaint is not currently in RESOLVED status")
    void confirmResolution_FailsIfNotResolved() {
        ConfirmResolutionRequest request = new ConfirmResolutionRequest("Premature confirm");
        when(complaintRepository.findById(200L)).thenReturn(Optional.of(inProgressComplaint));

        assertThrows(BadRequestException.class, () -> {
            lifecycleService.confirmResolution(200L, request, citizenPrincipal);
        });

        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin should successfully close resolved complaint with documentation remarks")
    void closeComplaint_AdminSuccess() {
        CloseComplaintRequest request = new CloseComplaintRequest("Superintendent verified site inspection.");

        when(complaintRepository.findById(100L)).thenReturn(Optional.of(resolvedComplaint));
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintResponse response = lifecycleService.closeComplaint(100L, request, adminPrincipal);

        assertNotNull(response);
        assertEquals(ComplaintStatus.CLOSED, response.getStatus());
        assertEquals("Superintendent verified site inspection.", response.getClosureRemarks());
        assertEquals(10L, response.getClosedById());
        assertNotNull(response.getClosedAt());

        verify(statusHistoryRepository).save(any(ComplaintStatusHistory.class));
    }

    @Test
    @DisplayName("Non-admin users attempting administrative closure should be rejected with 403/Unauthorized")
    void closeComplaint_FailsIfNotAdmin() {
        CloseComplaintRequest request = new CloseComplaintRequest("Attempted closure");

        assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.closeComplaint(100L, request, officerPrincipal);
        });

        assertThrows(UnauthorizedException.class, () -> {
            lifecycleService.closeComplaint(100L, request, citizenPrincipal);
        });

        verify(complaintRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin closure should fail if complaint is not resolved")
    void closeComplaint_FailsIfNotResolved() {
        CloseComplaintRequest request = new CloseComplaintRequest("Premature admin closure");
        when(complaintRepository.findById(200L)).thenReturn(Optional.of(inProgressComplaint));

        assertThrows(BadRequestException.class, () -> {
            lifecycleService.closeComplaint(200L, request, adminPrincipal);
        });

        verify(complaintRepository, never()).save(any());
    }
}
