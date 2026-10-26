package com.civiceye.ai.complaint;

import com.civiceye.ai.complaint.dto.ComplaintResponse;
import com.civiceye.ai.complaint.dto.CreateComplaintRequest;
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
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ComplaintService complaintService;

    private User sampleCitizen;
    private User otherCitizen;
    private User adminUser;
    private Complaint sampleComplaint;
    private CreateComplaintRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleCitizen = User.builder()
                .id(1L)
                .username("citizen_john")
                .email("john@civiceye.org")
                .fullName("John Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .build();

        otherCitizen = User.builder()
                .id(2L)
                .username("citizen_jane")
                .email("jane@civiceye.org")
                .fullName("Jane Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .build();

        adminUser = User.builder()
                .id(99L)
                .username("admin_alex")
                .email("admin@civiceye.org")
                .fullName("Alex Admin")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        sampleRequest = CreateComplaintRequest.builder()
                .title("Massive Pothole on Sector 4")
                .description("Dangerous deep pothole near crossroad causing traffic hazard")
                .category(ComplaintCategory.ROADS_AND_FOOTPATHS)
                .location("Sector 4, Main Market Road")
                .latitude(18.5204)
                .longitude(73.8567)
                .build();

        sampleComplaint = Complaint.builder()
                .id(100L)
                .complaintNumber("CE-2026-0001")
                .title(sampleRequest.getTitle())
                .description(sampleRequest.getDescription())
                .category(sampleRequest.getCategory())
                .status(ComplaintStatus.SUBMITTED)
                .priority(Priority.MEDIUM)
                .location(sampleRequest.getLocation())
                .latitude(sampleRequest.getLatitude())
                .longitude(sampleRequest.getLongitude())
                .citizen(sampleCitizen)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully create complaint without image, assign SUBMITTED status and generate complaint ID")
    void createComplaint_WithoutImage_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleCitizen));
        when(complaintRepository.findTopByComplaintNumberStartingWithOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        ComplaintResponse response = complaintService.createComplaint(sampleRequest, null, 1L);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Massive Pothole on Sector 4", response.getTitle());
        assertEquals(ComplaintCategory.ROADS_AND_FOOTPATHS, response.getCategory());
        assertEquals(ComplaintStatus.SUBMITTED, response.getStatus());
        assertEquals(Priority.MEDIUM, response.getPriority());
        assertTrue(response.getComplaintNumber().startsWith("CE-" + LocalDate.now().getYear() + "-0001"));
        assertEquals(1L, response.getCitizenId());
        assertNull(response.getImageUrl());
        verify(fileStorageService, never()).storeFile(any());
        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Should store image and attach imageUrl when complaint submitted with photo")
    void createComplaint_WithImage_Success() {
        MockMultipartFile imageFile = new MockMultipartFile("image", "evidence.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleCitizen));
        when(fileStorageService.storeFile(imageFile)).thenReturn("uuid_evidence.jpg");
        when(complaintRepository.findTopByComplaintNumberStartingWithOrderByIdDesc(anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint saved = invocation.getArgument(0);
            saved.setId(102L);
            return saved;
        });

        ComplaintResponse response = complaintService.createComplaint(sampleRequest, imageFile, 1L);

        assertNotNull(response);
        assertEquals("/api/files/uuid_evidence.jpg", response.getImageUrl());
        verify(fileStorageService).storeFile(imageFile);
    }

    @Test
    @DisplayName("Should increment sequence number when generating unique complaint ID")
    void generateComplaintNumber_IncrementsSequence() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "CE-" + currentYear + "-";
        Complaint existingComplaint = Complaint.builder()
                .complaintNumber(prefix + "0042")
                .build();
        when(complaintRepository.findTopByComplaintNumberStartingWithOrderByIdDesc(prefix))
                .thenReturn(Optional.of(existingComplaint));

        String generatedNumber = complaintService.generateComplaintNumber();

        assertEquals(prefix + "0043", generatedNumber);
    }

    @Test
    @DisplayName("Should allow citizen to view their own complaint by ID")
    void getComplaintById_CitizenViewsOwn_Success() {
        UserPrincipal principal = UserPrincipal.create(sampleCitizen);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        ComplaintResponse response = complaintService.getComplaintById(100L, principal);

        assertNotNull(response);
        assertEquals("CE-2026-0001", response.getComplaintNumber());
    }

    @Test
    @DisplayName("Acceptance Test AT-02: Should deny access when a citizen tries to view another citizen's complaint")
    void getComplaintById_CitizenViewsOthers_Denied() {
        UserPrincipal principal = UserPrincipal.create(otherCitizen); // ID 2 tries to view ID 1's complaint
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(UnauthorizedException.class, () -> complaintService.getComplaintById(100L, principal));
    }

    @Test
    @DisplayName("Should allow administrator to view any complaint")
    void getComplaintById_AdminViewsAny_Success() {
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        ComplaintResponse response = complaintService.getComplaintById(100L, adminPrincipal);

        assertNotNull(response);
        assertEquals("CE-2026-0001", response.getComplaintNumber());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when complaint does not exist")
    void getComplaintById_NotFound_ThrowsException() {
        UserPrincipal principal = UserPrincipal.create(sampleCitizen);
        when(complaintRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> complaintService.getComplaintById(999L, principal));
    }

    @Test
    @DisplayName("Should return citizen's complaints in order")
    void getMyComplaints_ReturnsList() {
        when(complaintRepository.findByCitizenIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(sampleComplaint));

        List<ComplaintResponse> complaints = complaintService.getMyComplaints(1L);

        assertEquals(1, complaints.size());
        assertEquals("CE-2026-0001", complaints.get(0).getComplaintNumber());
    }
}
