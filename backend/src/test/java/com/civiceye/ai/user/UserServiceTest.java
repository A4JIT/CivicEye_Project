package com.civiceye.ai.user;

import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.user.dto.CreateDepartmentStaffRequest;
import com.civiceye.ai.user.dto.DepartmentStaffResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Department sampleDepartment;
    private CreateDepartmentStaffRequest createStaffRequest;

    @BeforeEach
    void setUp() {
        sampleDepartment = Department.builder()
                .id(1L)
                .name("Public Works & Roads")
                .code("ROAD_WORKS")
                .isActive(true)
                .build();

        createStaffRequest = CreateDepartmentStaffRequest.builder()
                .username("officer_patil")
                .email("patil@pwd.gov.in")
                .password("Officer@123")
                .fullName("Sunil Patil")
                .departmentId(1L)
                .build();
    }

    @Test
    @DisplayName("Should successfully create department staff when valid request provided")
    void createDepartmentStaff_Success() {
        when(userRepository.existsByEmail("patil@pwd.gov.in")).thenReturn(false);
        when(userRepository.existsByUsername("officer_patil")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(passwordEncoder.encode("Officer@123")).thenReturn("$2a$10$encodedOfficerPass");

        User savedUser = User.builder()
                .id(50L)
                .username("officer_patil")
                .email("patil@pwd.gov.in")
                .fullName("Sunil Patil")
                .role(Role.DEPARTMENT_USER)
                .department(sampleDepartment)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        DepartmentStaffResponse response = userService.createDepartmentStaff(createStaffRequest);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("officer_patil", response.getUsername());
        assertEquals(Role.DEPARTMENT_USER, response.getRole());
        assertEquals(1L, response.getDepartmentId());
        assertEquals("Public Works & Roads", response.getDepartmentName());
        assertTrue(response.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department ID is invalid")
    void createDepartmentStaff_ThrowsResourceNotFound_WhenDeptNotFound() {
        when(userRepository.existsByEmail("patil@pwd.gov.in")).thenReturn(false);
        when(userRepository.existsByUsername("officer_patil")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createDepartmentStaff(createStaffRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when email or username is already taken")
    void createDepartmentStaff_ThrowsBadRequest_WhenEmailExists() {
        when(userRepository.existsByEmail("patil@pwd.gov.in")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createDepartmentStaff(createStaffRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list all department staff")
    void getAllDepartmentStaff_ReturnsList() {
        User staff = User.builder()
                .id(10L)
                .username("officer_1")
                .fullName("Officer One")
                .email("officer1@civiceye.com")
                .role(Role.DEPARTMENT_USER)
                .department(sampleDepartment)
                .isActive(true)
                .build();

        when(userRepository.findByRole(Role.DEPARTMENT_USER)).thenReturn(List.of(staff));

        List<DepartmentStaffResponse> list = userService.getAllDepartmentStaff();

        assertEquals(1, list.size());
        assertEquals("officer_1", list.get(0).getUsername());
        assertEquals("ROAD_WORKS", list.get(0).getDepartmentCode());
    }

    @Test
    @DisplayName("Should toggle staff status between active and inactive")
    void toggleDepartmentStaffStatus_Success() {
        User staff = User.builder()
                .id(10L)
                .username("officer_1")
                .role(Role.DEPARTMENT_USER)
                .department(sampleDepartment)
                .isActive(true)
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(staff));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentStaffResponse response = userService.toggleDepartmentStaffStatus(10L);

        assertFalse(response.isActive());
        verify(userRepository, times(1)).save(staff);
    }
}
