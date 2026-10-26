package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintTimelineResponse;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.UnauthorizedException;
import org.springframework.security.access.AccessDeniedException;
import com.civiceye.ai.security.UserPrincipal;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintHistoryServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Mock
    private ComplaintAssignmentRepository assignmentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private com.civiceye.ai.user.UserRepository userRepository;

    @InjectMocks
    private ComplaintHistoryService historyService;

    private User citizen1;
    private User citizen2;
    private User admin;
    private User officerRoad;
    private Department roadDept;
    private Department electricityDept;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        citizen1 = User.builder().id(1L).fullName("Citizen 1").role(Role.CITIZEN).build();
        citizen2 = User.builder().id(2L).fullName("Citizen 2").role(Role.CITIZEN).build();
        admin = User.builder().id(3L).fullName("Admin User").role(Role.ADMIN).build();

        roadDept = Department.builder().id(10L).name("Road Works").code("ROAD").build();
        electricityDept = Department.builder().id(20L).name("Electricity").code("ELEC").build();

        officerRoad = User.builder().id(4L).fullName("Road Officer").role(Role.DEPARTMENT_USER).department(roadDept).build();

        complaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-HIST-100")
                .title("Road Pothole")
                .description("Deep hole")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.IN_PROGRESS)
                .priority(Priority.HIGH)
                .location("Main Road")
                .citizen(citizen1)
                .department(roadDept)
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();
    }

    @Test
    @DisplayName("Reporting citizen can retrieve history for their own complaint")
    void citizenCanViewOwnHistory() {
        UserPrincipal citizenPrincipal = UserPrincipal.create(citizen1);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));
        when(statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());
        when(assignmentRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ComplaintTimelineResponse response = historyService.getComplaintHistory(100L, citizenPrincipal);

        assertNotNull(response);
        assertEquals("CE-HIST-100", response.getComplaintNumber());
        assertEquals(1, response.getEvents().size()); // 1 initial submission event
        assertEquals("SUBMITTED", response.getEvents().get(0).getEventType());
    }

    @Test
    @DisplayName("Citizen cannot view history of another citizen's complaint")
    void citizenCannotViewOtherCitizenHistory() {
        UserPrincipal otherCitizenPrincipal = UserPrincipal.create(citizen2);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                historyService.getComplaintHistory(100L, otherCitizenPrincipal));

        assertTrue(ex.getMessage().contains("You can only view history for your own complaints"));
    }

    @Test
    @DisplayName("Department user can view history for complaints assigned to their department")
    void departmentUserCanViewAssignedDepartmentHistory() {
        UserPrincipal officerPrincipal = UserPrincipal.create(officerRoad);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));
        when(userRepository.findById(4L)).thenReturn(Optional.of(officerRoad));
        when(statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());
        when(assignmentRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ComplaintTimelineResponse response = historyService.getComplaintHistory(100L, officerPrincipal);

        assertNotNull(response);
        assertEquals("CE-HIST-100", response.getComplaintNumber());
    }

    @Test
    @DisplayName("Department user cannot view history for complaints assigned to another department")
    void departmentUserCannotViewOtherDepartmentHistory() {
        User officerElectricity = User.builder().id(5L).fullName("Elec Officer").role(Role.DEPARTMENT_USER).department(electricityDept).build();
        UserPrincipal elecPrincipal = UserPrincipal.create(officerElectricity);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));
        when(userRepository.findById(5L)).thenReturn(Optional.of(officerElectricity));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                historyService.getComplaintHistory(100L, elecPrincipal));

        assertTrue(ex.getMessage().contains("assigned to your department"));
    }

    @Test
    @DisplayName("Admin user can view history for any complaint")
    void adminCanViewAnyHistory() {
        UserPrincipal adminPrincipal = UserPrincipal.create(admin);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(complaint));
        when(statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());
        when(assignmentRepository.findByComplaintIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ComplaintTimelineResponse response = historyService.getComplaintHistory(100L, adminPrincipal);

        assertNotNull(response);
        assertEquals("CE-HIST-100", response.getComplaintNumber());
    }

    @Test
    @DisplayName("Recording assignment deactivates previous active assignment and logs new one")
    void recordAssignmentDeactivatesPrevious() {
        ComplaintAssignment previous = ComplaintAssignment.builder()
                .id(1L)
                .complaint(complaint)
                .department(roadDept)
                .isActive(true)
                .build();

        when(assignmentRepository.findByComplaintIdAndIsActiveTrue(100L)).thenReturn(Optional.of(previous));
        when(assignmentRepository.save(any(ComplaintAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        ComplaintAssignment newAssignment = historyService.recordAssignment(complaint, electricityDept, null, admin, "Handover to electricity");

        assertFalse(previous.getIsActive());
        assertNotNull(previous.getCompletedAt());
        assertTrue(newAssignment.getIsActive());
        assertEquals(electricityDept.getId(), newAssignment.getDepartment().getId());
        verify(assignmentRepository).save(previous);
    }
}
