package com.civiceye.ai.complaint;

import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ComplaintStatusHistoryRepositoryTest {

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User citizen;
    private User admin;
    private User officer;
    private Department roadDept;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        citizen = userRepository.save(User.builder()
                .username("history_citizen")
                .email("history_citizen@example.com")
                .password("encoded_pass")
                .fullName("Citizen History Tester")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        admin = userRepository.save(User.builder()
                .username("history_admin")
                .email("history_admin@example.com")
                .password("encoded_pass")
                .fullName("Admin History Reviewer")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        roadDept = departmentRepository.save(Department.builder()
                .code("ROAD_MAINT")
                .name("Road Maintenance Unit")
                .description("City road maintenance")
                .isActive(true)
                .build());

        officer = userRepository.save(User.builder()
                .username("history_officer")
                .email("history_officer@example.com")
                .password("encoded_pass")
                .fullName("PWD Field Officer")
                .role(Role.DEPARTMENT_USER)
                .department(roadDept)
                .isActive(true)
                .build());

        complaint = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-HIST-001")
                .title("Damaged storm drain grate")
                .description("Pedestrian trip hazard at corner of 5th Ave.")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.HIGH)
                .location("5th Ave & Elm St")
                .citizen(citizen)
                .department(roadDept)
                .build());
    }

    @Test
    @DisplayName("Should save and retrieve status history records in chronological ascending order")
    void testChronologicalAscendingOrdering() {
        LocalDateTime baseTime = LocalDateTime.now().minusHours(5);

        ComplaintStatusHistory step1 = statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(null)
                .toStatus(ComplaintStatus.SUBMITTED)
                .changedBy(citizen)
                .remarks("Citizen submitted complaint")
                .createdAt(baseTime.plusMinutes(10))
                .build());

        ComplaintStatusHistory step2 = statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.SUBMITTED)
                .toStatus(ComplaintStatus.UNDER_REVIEW)
                .changedBy(admin)
                .remarks("Admin screening")
                .createdAt(baseTime.plusMinutes(30))
                .build());

        ComplaintStatusHistory step3 = statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.UNDER_REVIEW)
                .toStatus(ComplaintStatus.ASSIGNED)
                .changedBy(admin)
                .remarks("Dispatched to road maintenance unit")
                .createdAt(baseTime.plusHours(1))
                .build());

        List<ComplaintStatusHistory> historyAsc = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId());

        assertEquals(3, historyAsc.size());
        assertEquals(ComplaintStatus.SUBMITTED, historyAsc.get(0).getToStatus());
        assertEquals(ComplaintStatus.UNDER_REVIEW, historyAsc.get(1).getToStatus());
        assertEquals(ComplaintStatus.ASSIGNED, historyAsc.get(2).getToStatus());
        assertEquals(citizen.getId(), historyAsc.get(0).getChangedBy().getId());
        assertEquals(admin.getId(), historyAsc.get(1).getChangedBy().getId());
    }

    @Test
    @DisplayName("Should retrieve status history records in chronological descending order")
    void testChronologicalDescendingOrdering() {
        LocalDateTime baseTime = LocalDateTime.now().minusHours(3);

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(null)
                .toStatus(ComplaintStatus.SUBMITTED)
                .changedBy(citizen)
                .remarks("Submitted")
                .createdAt(baseTime)
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.SUBMITTED)
                .toStatus(ComplaintStatus.UNDER_REVIEW)
                .changedBy(admin)
                .remarks("Under Review")
                .createdAt(baseTime.plusHours(1))
                .build());

        List<ComplaintStatusHistory> historyDesc = statusHistoryRepository.findByComplaintIdOrderByCreatedAtDesc(complaint.getId());

        assertEquals(2, historyDesc.size());
        assertEquals(ComplaintStatus.UNDER_REVIEW, historyDesc.get(0).getToStatus());
        assertEquals(ComplaintStatus.SUBMITTED, historyDesc.get(1).getToStatus());
    }

    @Test
    @DisplayName("Should maintain audit actor integrity across entire lifecycle to closure")
    void testCompleteLifecycleAuditTrail() {
        LocalDateTime now = LocalDateTime.now();

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(null)
                .toStatus(ComplaintStatus.SUBMITTED)
                .changedBy(citizen)
                .remarks("Report created")
                .createdAt(now.minusHours(4))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.SUBMITTED)
                .toStatus(ComplaintStatus.UNDER_REVIEW)
                .changedBy(admin)
                .remarks("Admin approved")
                .createdAt(now.minusHours(3))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.UNDER_REVIEW)
                .toStatus(ComplaintStatus.ASSIGNED)
                .changedBy(admin)
                .remarks("Assigned to officer")
                .createdAt(now.minusHours(2).plusMinutes(30))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.ASSIGNED)
                .toStatus(ComplaintStatus.IN_PROGRESS)
                .changedBy(officer)
                .remarks("Grate replacement crew on site")
                .createdAt(now.minusHours(2))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.IN_PROGRESS)
                .toStatus(ComplaintStatus.RESOLVED)
                .changedBy(officer)
                .remarks("New ductile iron grate installed and anchored.")
                .resolutionImageUrl("https://images.example.com/proof-grate.jpg")
                .createdAt(now.minusHours(1))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.RESOLVED)
                .toStatus(ComplaintStatus.CLOSED)
                .changedBy(admin)
                .remarks("Audit verified. Closed.")
                .createdAt(now.minusMinutes(30))
                .build());

        List<ComplaintStatusHistory> history = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId());

        assertEquals(6, history.size());
        assertEquals(6, statusHistoryRepository.countByComplaintId(complaint.getId()));

        ComplaintStatusHistory resolvedEntry = history.get(4);
        assertEquals(ComplaintStatus.RESOLVED, resolvedEntry.getToStatus());
        assertEquals("https://images.example.com/proof-grate.jpg", resolvedEntry.getResolutionImageUrl());
        assertEquals(officer.getId(), resolvedEntry.getChangedBy().getId());

        ComplaintStatusHistory closedEntry = history.get(5);
        assertEquals(ComplaintStatus.CLOSED, closedEntry.getToStatus());
        assertEquals(admin.getId(), closedEntry.getChangedBy().getId());
        assertEquals("Audit verified. Closed.", closedEntry.getRemarks());
    }

    @Test
    @DisplayName("Should capture rejection audit trail with reason and administrator identity")
    void testRejectionAuditTrail() {
        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(null)
                .toStatus(ComplaintStatus.SUBMITTED)
                .changedBy(citizen)
                .remarks("New complaint")
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        statusHistoryRepository.save(ComplaintStatusHistory.builder()
                .complaint(complaint)
                .fromStatus(ComplaintStatus.SUBMITTED)
                .toStatus(ComplaintStatus.REJECTED)
                .changedBy(admin)
                .remarks("Duplicate entry of CE-2026-0941")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());

        List<ComplaintStatusHistory> history = statusHistoryRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId());

        assertEquals(2, history.size());
        ComplaintStatusHistory rejection = history.get(1);
        assertEquals(ComplaintStatus.REJECTED, rejection.getToStatus());
        assertEquals(admin.getId(), rejection.getChangedBy().getId());
        assertEquals("Duplicate entry of CE-2026-0941", rejection.getRemarks());
    }
}
