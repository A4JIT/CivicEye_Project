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

    @org.springframework.beans.factory.annotation.Value("${app.seeder.seed-complaints:true}")
    private boolean seedComplaints;

    public DatabaseSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          DepartmentRepository departmentRepository,
                          ComplaintRepository complaintRepository,
                          ComplaintFeedbackRepository feedbackRepository,
                          ComplaintStatusHistoryRepository statusHistoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.complaintRepository = complaintRepository;
        this.feedbackRepository = feedbackRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed base users
        User admin = syncUser("admin@civiceye.com", "admin", "CivicEye Administrator", Role.ADMIN);
        User citizen = syncUser("citizen@civiceye.com", "citizen_jane", "Jane Citizen", Role.CITIZEN);
        User officer = syncUser("dept_roads@civiceye.com", "officer_sharma", "Rajesh Sharma (PWD Officer)", Role.DEPARTMENT_USER);

        // 2. Conditionally seed Phase 8 test departments & complaints
        if (seedComplaints) {
            Department roadDept = syncDepartment("ROAD_WORKS", "Roads & Infrastructure", "Responsible for road maintenance, potholes, and sidewalks.");
            Department electricityDept = syncDepartment("ELECTRICITY", "Streetlights & Electricity", "Responsible for street lighting and public electric infrastructure.");

            if (officer != null && (officer.getDepartment() == null || !officer.getDepartment().getId().equals(roadDept.getId()))) {
                officer.setDepartment(roadDept);
                userRepository.save(officer);
            }

            seedResolvedComplaint(citizen, roadDept, officer);
            seedClosedComplaintWithFeedback(citizen, electricityDept, admin, officer);
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
}
