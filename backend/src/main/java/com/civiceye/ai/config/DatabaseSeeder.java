package com.civiceye.ai.config;

import com.civiceye.ai.complaint.*;
import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintFeedbackRepository feedbackRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final ComplaintAssignmentRepository assignmentRepository;

    @org.springframework.beans.factory.annotation.Value("${app.seeder.seed-complaints:true}")
    private boolean seedComplaints;

    public DatabaseSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          DepartmentRepository departmentRepository,
                          ComplaintRepository complaintRepository,
                          ComplaintFeedbackRepository feedbackRepository,
                          ComplaintStatusHistoryRepository statusHistoryRepository,
                          ComplaintAssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.complaintRepository = complaintRepository;
        this.feedbackRepository = feedbackRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed base users
        User admin = syncUser("admin@civiceye.com", "admin", "CivicEye Administrator", Role.ADMIN);
        User citizen = syncUser("citizen@civiceye.com", "citizen_jane", "Jane Citizen", Role.CITIZEN);
        User officer = syncUser("dept_roads@civiceye.com", "officer_sharma", "Rajesh Sharma (PWD Officer)", Role.DEPARTMENT_USER);

        // 2. Conditionally seed Phase 8 & Phase 9 test departments & complaints
        if (seedComplaints) {
            Department roadDept = syncDepartment("ROAD_WORKS", "Roads & Infrastructure", "Responsible for road maintenance, potholes, and sidewalks.");
            Department electricityDept = syncDepartment("ELECTRICITY", "Streetlights & Electricity", "Responsible for street lighting and public electric infrastructure.");
            Department waterDept = syncDepartment("WATER_SUPPLY", "Water & Sewerage", "Responsible for municipal water supply lines and stormwater drains.");

            if (officer != null && (officer.getDepartment() == null || !officer.getDepartment().getId().equals(roadDept.getId()))) {
                officer.setDepartment(roadDept);
                userRepository.save(officer);
            }

            seedResolvedComplaint(citizen, roadDept, officer);
            seedClosedComplaintWithFeedback(citizen, electricityDept, admin, officer);
            seedMultiStepLifecycleComplaint(citizen, waterDept, admin, officer);
            seedRejectedComplaintWithAudit(citizen, admin);
            seedReassignedComplaintWithHandover(citizen, roadDept, electricityDept, admin, officer);
        }
    }

    private User syncUser(String email, String username, String fullName, Role role) {
        return userRepository.findByEmail(email).map(user -> {
            if (!passwordEncoder.matches("Password@123", user.getPassword())) {
                user.setPassword(passwordEncoder.encode("Password@123"));
                user = userRepository.save(user);
                log.info("Updated password for test account: {}", email);
            }
            return user;
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .password(passwordEncoder.encode("Password@123"))
                    .role(role)
                    .isActive(true)
                    .build();
            User saved = userRepository.save(newUser);
            log.info("Created missing default test account: {}", email);
            return saved;
        });
    }

    private Department syncDepartment(String code, String name, String description) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department dept = Department.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .contactEmail(code.toLowerCase() + "@civiceye.gov")
                    .contactPhone("+91-11-23456789")
                    .isActive(true)
                    .build();
            Department saved = departmentRepository.save(dept);
            log.info("Created missing department: {} ({})", name, code);
            return saved;
        });
    }

    private void seedResolvedComplaint(User citizen, Department department, User officer) {
        String complaintNumber = "CE-2026-1001";
        if (complaintRepository.findByComplaintNumber(complaintNumber).isEmpty()) {
            Complaint complaint = Complaint.builder()
                    .complaintNumber(complaintNumber)
                    .title("Hazardous Pothole on MG Road Junction")
                    .description("Large pothole near metro pillar 142 causing vehicle damage and traffic bottleneck.")
                    .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                    .status(ComplaintStatus.RESOLVED)
                    .priority(Priority.HIGH)
                    .location("MG Road, Near Metro Pillar 142")
                    .latitude(18.5204)
                    .longitude(73.8567)
                    .citizen(citizen)
                    .department(department)
                    .assignedTo(officer)
                    .startedWorkingAt(LocalDateTime.now().minusDays(2))
                    .progressNotes("Asphalt resurfacing crew dispatched with roller.")
                    .resolvedAt(LocalDateTime.now().minusHours(6))
                    .resolutionNotes("Pothole cleaned, filled with hot-mix asphalt, compacted and levelled. Road cleared for traffic.")
                    .resolutionImageUrl("https://images.unsplash.com/photo-1541888946425-d0fbb1861593?w=800")
                    .citizenConfirmedResolution(false)
                    .build();

            Complaint saved = complaintRepository.save(complaint);

            ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.IN_PROGRESS)
                    .toStatus(ComplaintStatus.RESOLVED)
                    .changedBy(officer)
                    .remarks("Resolution complete with proof photos.")
                    .resolutionImageUrl("https://images.unsplash.com/photo-1541888946425-d0fbb1861593?w=800")
                    .createdAt(LocalDateTime.now().minusHours(6))
                    .build();
            statusHistoryRepository.save(history);

            log.info("Seeded Phase 8 RESOLVED complaint: {}", complaintNumber);
        }
    }

    private void seedClosedComplaintWithFeedback(User citizen, Department department, User admin, User officer) {
        String complaintNumber = "CE-2026-1002";
        if (complaintRepository.findByComplaintNumber(complaintNumber).isEmpty()) {
            Complaint complaint = Complaint.builder()
                    .complaintNumber(complaintNumber)
                    .title("Defective Streetlight Luminaire on Park Street")
                    .description("Streetlight flickering continuously and pedestrian walkway in total darkness.")
                    .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                    .status(ComplaintStatus.CLOSED)
                    .priority(Priority.MEDIUM)
                    .location("Park Street, Cross Lane 4")
                    .latitude(18.5314)
                    .longitude(73.8446)
                    .citizen(citizen)
                    .department(department)
                    .assignedTo(officer)
                    .startedWorkingAt(LocalDateTime.now().minusDays(4))
                    .progressNotes("Electrician team dispatched with replacement luminaire.")
                    .resolvedAt(LocalDateTime.now().minusDays(1))
                    .resolutionNotes("Replaced defective sodium luminaire with new 90W LED fixture. Wiring and ground connections tested.")
                    .resolutionImageUrl("https://images.unsplash.com/photo-1509114397022-ed747cca3f65?w=800")
                    .citizenConfirmedResolution(true)
                    .closedAt(LocalDateTime.now().minusHours(4))
                    .closedBy(admin)
                    .closureRemarks("Citizen verified resolution and awarded 5-star rating. Officially closed.")
                    .build();

            Complaint saved = complaintRepository.save(complaint);

            // Seed 5-star feedback
            ComplaintFeedback feedback = ComplaintFeedback.builder()
                    .complaint(saved)
                    .citizen(citizen)
                    .department(department)
                    .rating(5)
                    .comments("Outstanding turnaround speed! Walkway is bright and safe now. Thank you CivicEye!")
                    .createdAt(LocalDateTime.now().minusHours(5))
                    .build();
            ComplaintFeedback savedFeedback = feedbackRepository.save(feedback);
            saved.setFeedback(savedFeedback);

            ComplaintStatusHistory history = ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.RESOLVED)
                    .toStatus(ComplaintStatus.CLOSED)
                    .changedBy(admin)
                    .remarks("Administrative closure following citizen confirmation and feedback.")
                    .createdAt(LocalDateTime.now().minusHours(4))
                    .build();
            statusHistoryRepository.save(history);

            log.info("Seeded Phase 8 CLOSED complaint with feedback: {}", complaintNumber);
        }
    }

    private void seedMultiStepLifecycleComplaint(User citizen, Department department, User admin, User officer) {
        String complaintNumber = "CE-2026-1003";
        if (complaintRepository.findByComplaintNumber(complaintNumber).isEmpty()) {
            Complaint complaint = Complaint.builder()
                    .complaintNumber(complaintNumber)
                    .title("Major Water Main Rupture Flooding Subhash Nagar Market")
                    .description("Large 12-inch water main ruptured under the roadway, causing major flooding, low water pressure in 400 households, and road erosion.")
                    .category(ComplaintCategory.WATER_SUPPLY_AND_LEAKAGE)
                    .status(ComplaintStatus.CLOSED)
                    .priority(Priority.CRITICAL)
                    .location("Subhash Nagar Central Market, Lane 3")
                    .latitude(18.5280)
                    .longitude(73.8500)
                    .citizen(citizen)
                    .department(department)
                    .assignedTo(officer)
                    .startedWorkingAt(LocalDateTime.now().minusDays(3))
                    .progressNotes("Excavation completed. Burst cast-iron pipe segment cut out and replaced with ductile iron sleeve.")
                    .resolvedAt(LocalDateTime.now().minusDays(1))
                    .resolutionNotes("Water main fully restored, pressure tested to 6 bar with zero leaks. Road trench backfilled and asphalted.")
                    .resolutionImageUrl("https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=800")
                    .citizenConfirmedResolution(true)
                    .closedAt(LocalDateTime.now().minusHours(8))
                    .closedBy(admin)
                    .closureRemarks("Emergency repair audited by municipal civil engineering supervisor. Officially closed.")
                    .build();

            Complaint saved = complaintRepository.save(complaint);

            // Seed full multi-step status history audit trail
            // 1. SUBMITTED
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(null)
                    .toStatus(ComplaintStatus.SUBMITTED)
                    .changedBy(citizen)
                    .remarks("Citizen submitted emergency complaint with geo-location and photo evidence.")
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build());

            // 2. UNDER_REVIEW
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.SUBMITTED)
                    .toStatus(ComplaintStatus.UNDER_REVIEW)
                    .changedBy(admin)
                    .remarks("Admin triage: Validated emergency hazard markers and escalated priority to CRITICAL.")
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .build());

            // 3. ASSIGNED
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.UNDER_REVIEW)
                    .toStatus(ComplaintStatus.ASSIGNED)
                    .changedBy(admin)
                    .remarks("Assigned to Water Supply & Sewerage rapid response engineering crew.")
                    .createdAt(LocalDateTime.now().minusDays(4).plusHours(2))
                    .build());

            // 4. IN_PROGRESS
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.ASSIGNED)
                    .toStatus(ComplaintStatus.IN_PROGRESS)
                    .changedBy(officer)
                    .remarks("Emergency crew on site. Isolation gate valves closed to stop flooding.")
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .build());

            // 5. RESOLVED
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.IN_PROGRESS)
                    .toStatus(ComplaintStatus.RESOLVED)
                    .changedBy(officer)
                    .remarks("Pipe replaced, pressure tested at 6 bar, roadway reinstated.")
                    .resolutionImageUrl("https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=800")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            // 6. CLOSED
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.RESOLVED)
                    .toStatus(ComplaintStatus.CLOSED)
                    .changedBy(admin)
                    .remarks("Field inspection completed by municipal supervisor. Resolution confirmed and complaint closed.")
                    .createdAt(LocalDateTime.now().minusHours(8))
                    .build());

            // Seed Assignment record
            assignmentRepository.save(ComplaintAssignment.builder()
                    .complaint(saved)
                    .department(department)
                    .assignedTo(officer)
                    .assignedBy(admin)
                    .instructions("URGENT: Isolate burst line immediately and coordinate traffic diversion.")
                    .isActive(false)
                    .createdAt(LocalDateTime.now().minusDays(4).plusHours(2))
                    .completedAt(LocalDateTime.now().minusDays(1))
                    .build());

            log.info("Seeded Phase 9 Multi-Step Lifecycle complaint: {}", complaintNumber);
        }
    }

    private void seedRejectedComplaintWithAudit(User citizen, User admin) {
        String complaintNumber = "CE-2026-1004";
        if (complaintRepository.findByComplaintNumber(complaintNumber).isEmpty()) {
            Complaint complaint = Complaint.builder()
                    .complaintNumber(complaintNumber)
                    .title("Unverified Construction Noise on Private Property")
                    .description("Neighbor conducting interior remodeling at 2 PM causing disturbance.")
                    .category(ComplaintCategory.OTHER)
                    .status(ComplaintStatus.REJECTED)
                    .priority(Priority.LOW)
                    .location("Flat 402, Shanti Heights, Shivaji Road")
                    .latitude(18.5150)
                    .longitude(73.8420)
                    .citizen(citizen)
                    .rejectionReason("Non-civic municipal issue; private residential dispute outside civic jurisdiction. Please contact local society management.")
                    .build();

            Complaint saved = complaintRepository.save(complaint);

            // Audit Trail
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(null)
                    .toStatus(ComplaintStatus.SUBMITTED)
                    .changedBy(citizen)
                    .remarks("Complaint submitted by citizen.")
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.SUBMITTED)
                    .toStatus(ComplaintStatus.UNDER_REVIEW)
                    .changedBy(admin)
                    .remarks("Admin screening against municipal criteria.")
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.UNDER_REVIEW)
                    .toStatus(ComplaintStatus.REJECTED)
                    .changedBy(admin)
                    .remarks("Rejected: Non-civic municipal issue; private residential dispute outside civic jurisdiction. Please contact local society management.")
                    .createdAt(LocalDateTime.now().minusDays(2).plusHours(1))
                    .build());

            log.info("Seeded Phase 9 Rejected complaint with audit trail: {}", complaintNumber);
        }
    }

    private void seedReassignedComplaintWithHandover(User citizen, Department roadDept, Department electricityDept, User admin, User officer) {
        String complaintNumber = "CE-2026-1005";
        if (complaintRepository.findByComplaintNumber(complaintNumber).isEmpty()) {
            Complaint complaint = Complaint.builder()
                    .complaintNumber(complaintNumber)
                    .title("Exposed High-Voltage Cable in Roadside Drainage Trench")
                    .description("Trench dug for sidewalk paving exposed damaged 11kV electrical conduit. Water accumulating around cables.")
                    .category(ComplaintCategory.STREETLIGHTS_AND_ELECTRICITY)
                    .status(ComplaintStatus.IN_PROGRESS)
                    .priority(Priority.CRITICAL)
                    .location("Station Road Outer Ring, Pillar 48")
                    .latitude(18.5350)
                    .longitude(73.8650)
                    .citizen(citizen)
                    .department(electricityDept)
                    .assignedTo(officer)
                    .startedWorkingAt(LocalDateTime.now().minusHours(12))
                    .progressNotes("Power utility team deployed with insulating mats and conduit sleeving.")
                    .build();

            Complaint saved = complaintRepository.save(complaint);

            // Status transitions
            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(null)
                    .toStatus(ComplaintStatus.SUBMITTED)
                    .changedBy(citizen)
                    .remarks("Reported exposed wires in open drain trench.")
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.SUBMITTED)
                    .toStatus(ComplaintStatus.UNDER_REVIEW)
                    .changedBy(admin)
                    .remarks("Admin review initiated.")
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.UNDER_REVIEW)
                    .toStatus(ComplaintStatus.ASSIGNED)
                    .changedBy(admin)
                    .remarks("Initial assignment to Roads & Infrastructure department for sidewalk trench.")
                    .createdAt(LocalDateTime.now().minusDays(2).plusHours(2))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.ASSIGNED)
                    .toStatus(ComplaintStatus.ASSIGNED)
                    .changedBy(admin)
                    .remarks("Reassigned to Streetlights & Electricity department: Field engineer reported live 11kV power conduit requiring electrical utility shutdown.")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            statusHistoryRepository.save(ComplaintStatusHistory.builder()
                    .complaint(saved)
                    .fromStatus(ComplaintStatus.ASSIGNED)
                    .toStatus(ComplaintStatus.IN_PROGRESS)
                    .changedBy(officer)
                    .remarks("Emergency electrical safety protocol initiated; barricades installed.")
                    .createdAt(LocalDateTime.now().minusHours(12))
                    .build());

            // 1st assignment: Road dept (completed / reassigned)
            assignmentRepository.save(ComplaintAssignment.builder()
                    .complaint(saved)
                    .department(roadDept)
                    .assignedBy(admin)
                    .instructions("Inspect sidewalk trench condition.")
                    .isActive(false)
                    .createdAt(LocalDateTime.now().minusDays(2).plusHours(2))
                    .completedAt(LocalDateTime.now().minusDays(1))
                    .build());

            // 2nd assignment: Electricity dept (currently active)
            assignmentRepository.save(ComplaintAssignment.builder()
                    .complaint(saved)
                    .department(electricityDept)
                    .assignedTo(officer)
                    .assignedBy(admin)
                    .instructions("CRITICAL HANDOVER: Isolate 11kV line, sleeve conduit, and verify safety before road trench backfilling.")
                    .isActive(true)
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            log.info("Seeded Phase 9 Reassigned complaint with handover notes: {}", complaintNumber);
        }
    }
}
