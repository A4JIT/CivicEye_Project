package com.civiceye.ai.user;

import com.civiceye.ai.department.Department;
import com.civiceye.ai.department.DepartmentRepository;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.ResourceNotFoundException;
import com.civiceye.ai.user.dto.CreateDepartmentStaffRequest;
import com.civiceye.ai.user.dto.DepartmentStaffResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DepartmentStaffResponse createDepartmentStaff(CreateDepartmentStaffRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(Role.DEPARTMENT_USER)
                .department(department)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return DepartmentStaffResponse.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public List<DepartmentStaffResponse> getAllDepartmentStaff() {
        return userRepository.findByRole(Role.DEPARTMENT_USER).stream()
                .map(DepartmentStaffResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DepartmentStaffResponse toggleDepartmentStaffStatus(Long staffId) {
        User user = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + staffId));

        if (user.getRole() != Role.DEPARTMENT_USER) {
            throw new BadRequestException("Only department staff status can be toggled through this endpoint");
        }

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);
        return DepartmentStaffResponse.fromEntity(updated);
    }
}
