package com.civiceye.ai.complaint;

import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminComplaintMonitoringQueryTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User citizen1;
    private User citizen2;
    private User admin;
    private User roadOfficer;
    private User elecOfficer;
    private Department roadDept;
    private Department electricityDept;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        statusHistoryRepository.deleteAll();
        complaintRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        // Seed users
        citizen1 = userRepository.save(User.builder()
                .username("citizen_rahul")
                .email("rahul@example.com")
                .password("encoded_pass")
                .fullName("Rahul Verma")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        citizen2 = userRepository.save(User.builder()
                .username("citizen_anita")
                .email("anita@example.com")
                .password("encoded_pass")
                .fullName("Anita Desai")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());

        admin = userRepository.save(User.builder()
                .username("admin_monitoring")
                .email("admin_monitoring@example.com")
                .password("encoded_pass")
                .fullName("Chief Administrative Officer")
                .role(Role.ADMIN)
                .isActive(true)
                .build());

        roadDept = departmentRepository.save(Department.builder()
                .code("ROADS_DEPT")
                .name("Roads & Civil Works")
                .description("Road maintenance and paving")
                .isActive(true)
                .build());

        electricityDept = departmentRepository.save(Department.builder()
                .code("POWER_DEPT")
                .name("Power & Lighting")
                .description("Street lighting and municipal power")
                .isActive(true)
                .build());

        roadOfficer = userRepository.save(User.builder()
                .username("road_officer")
                .email("road_officer@example.com")
                .password("encoded_pass")
                .fullName("Vikram Singh")
                .role(Role.DEPARTMENT_USER)
                .department(roadDept)
                .isActive(true)
                .build());

        elecOfficer = userRepository.save(User.builder()
                .username("elec_officer")
                .email("elec_officer@example.com")
                .password("encoded_pass")
                .fullName("Pooja Iyer")
                .role(Role.DEPARTMENT_USER)
                .department(electricityDept)
                .isActive(true)
                .build());

        seedMonitoringDataset();
    }

    private void seedMonitoringDataset() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Complaint A: Roads, SUBMITTED, HIGH, 10 days ago
        Complaint c1 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-MON-101")
                .title("Massive Pothole outside High School")
                .description("School bus damaged its axle on the crater.")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.HIGH)
                .location("Model Town Sector 4")
                .citizen(citizen1)
                .createdAt(now.minusDays(10))
                .build());

        // 2. Complaint B: Roads, IN_PROGRESS, CRITICAL, 5 days ago, assigned to roadOfficer
        Complaint c2 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-MON-102")
                .title("Flyover expansion joint separation")
                .description("Structural joint gap widening under traffic.")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .status(ComplaintStatus.IN_PROGRESS)
                .priority(Priority.CRITICAL)
                .location("Central Flyover Ramp B")
                .citizen(citizen2)
                .department(roadDept)
                .assignedTo(roadOfficer)
                .startedWorkingAt(now.minusDays(3))
                .createdAt(now.minusDays(5))
                .build());

        assignmentRepository.save(ComplaintAssignment.builder()
                .complaint(c2)
                .department(roadDept)
                .assignedTo(roadOfficer)
                .assignedBy(admin)
                .instructions("Deploy structural inspection unit.")
                .isActive(true)
                .createdAt(now.minusDays(4))
                .build());

        // 3. Complaint C: Electricity, RESOLVED, MEDIUM, 3 days ago, assigned to elecOfficer
        Complaint c3 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-MON-103")
                .title("Flickering LED streetlights across Block D")
                .description("Ten consecutive streetlights shut off at night.")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .status(ComplaintStatus.RESOLVED)
                .priority(Priority.MEDIUM)
                .location("Block D Main Boulevard")
                .citizen(citizen1)
                .department(electricityDept)
                .assignedTo(elecOfficer)
                .resolvedAt(now.minusDays(1))
                .resolutionNotes("Replaced control timer relay.")
                .resolutionImageUrl("https://images.example.com/timer-fixed.jpg")
                .createdAt(now.minusDays(3))
                .build());

        assignmentRepository.save(ComplaintAssignment.builder()
                .complaint(c3)
                .department(electricityDept)
                .assignedTo(elecOfficer)
                .assignedBy(admin)
                .instructions("Replace timer switch.")
                .isActive(true)
                .createdAt(now.minusDays(2))
                .completedAt(now.minusDays(1))
                .build());

        // 4. Complaint D: Electricity, CLOSED, LOW, 2 days ago
        Complaint c4 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-MON-104")
                .title("Open meter box door")
                .description("Meter box latch loose.")
                .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                .status(ComplaintStatus.CLOSED)
                .priority(Priority.LOW)
                .location("Park Lane No 2")
                .citizen(citizen2)
                .department(electricityDept)
                .assignedTo(elecOfficer)
                .resolvedAt(now.minusDays(1))
                .closedAt(now.minusHours(4))
                .closedBy(admin)
                .closureRemarks("Secured and verified.")
                .citizenConfirmedResolution(true)
                .createdAt(now.minusDays(2))
                .build());

        // 5. Complaint E: Other, REJECTED, LOW, 1 day ago
        Complaint c5 = complaintRepository.save(Complaint.builder()
                .complaintNumber("CE-MON-105")
                .title("Stray pet dispute with neighbor")
                .description("Private domestic dispute.")
                .category(ComplaintCategory.OTHER)
                .status(ComplaintStatus.REJECTED)
                .priority(Priority.LOW)
                .location("Apartment 102")
                .citizen(citizen1)
                .rejectionReason("Private dispute outside civic domain.")
                .createdAt(now.minusDays(1))
                .build());
    }

    @Test
    @DisplayName("Should filter complaints by status using JPA Specification")
    void testFilterByStatus() {
        Specification<Complaint> spec = (root, query, cb) -> cb.equal(root.get("status"), ComplaintStatus.IN_PROGRESS);
        List<Complaint> results = complaintRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("CE-MON-102", results.get(0).getComplaintNumber());
    }

    @Test
    @DisplayName("Should filter complaints by department ID")
    void testFilterByDepartment() {
        Specification<Complaint> spec = (root, query, cb) -> cb.equal(root.get("department").get("id"), electricityDept.getId());
        List<Complaint> results = complaintRepository.findAll(spec);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getDepartment().getCode().equals("POWER_DEPT")));
    }

    @Test
    @DisplayName("Should filter complaints by category and priority")
    void testFilterByCategoryAndPriority() {
        Specification<Complaint> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("category"), ComplaintCategory.ROADS_AND_FOOTPATHS),
                cb.equal(root.get("priority"), Priority.CRITICAL)
        );
        List<Complaint> results = complaintRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("CE-MON-102", results.get(0).getComplaintNumber());
    }

    @Test
    @DisplayName("Should search complaints by keyword in title, description, or complaint number")
    void testSearchQueryMatching() {
        String keyword = "%flyover%";
        Specification<Complaint> spec = (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), keyword.toLowerCase()),
                cb.like(cb.lower(root.get("description")), keyword.toLowerCase()),
                cb.like(cb.lower(root.get("complaintNumber")), keyword.toLowerCase())
        );
        List<Complaint> results = complaintRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("CE-MON-102", results.get(0).getComplaintNumber());
    }

    @Test
    @DisplayName("Should support multi-criteria administrative monitoring specification with pagination and sorting")
    void testMultiCriteriaMonitoringSpecification() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(7);
        LocalDateTime toDate = LocalDateTime.now();

        Specification<Complaint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Department filter
            predicates.add(cb.equal(root.get("department").get("id"), electricityDept.getId()));
            // Date range filter
            predicates.add(cb.between(root.get("createdAt"), fromDate, toDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Complaint> page = complaintRepository.findAll(
                spec,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        assertEquals(2, page.getTotalElements());
        assertEquals("CE-MON-104", page.getContent().get(0).getComplaintNumber());
        assertEquals("CE-MON-103", page.getContent().get(1).getComplaintNumber());
    }

    @Test
    @DisplayName("Should query assignment history and active assignments correctly")
    void testAssignmentRepositoryQueries() {
        Complaint c2 = complaintRepository.findByComplaintNumber("CE-MON-102").orElseThrow();

        // Check active assignment
        var activeOpt = assignmentRepository.findByComplaintIdAndIsActiveTrue(c2.getId());
        assertTrue(activeOpt.isPresent());
        assertEquals(roadOfficer.getId(), activeOpt.get().getAssignedTo().getId());
        assertTrue(activeOpt.get().getIsActive());

        // Check active assignments by department
        List<ComplaintAssignment> roadActive = assignmentRepository.findByDepartmentIdAndIsActiveTrue(roadDept.getId());
        assertEquals(1, roadActive.size());

        // Check active assignments count by department
        long count = assignmentRepository.countByDepartmentIdAndIsActiveTrue(roadDept.getId());
        assertEquals(1, count);
    }
}
