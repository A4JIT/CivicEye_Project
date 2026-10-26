package com.civiceye.ai.user.dto;

import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStaffResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private boolean isActive;
    private LocalDateTime createdAt;

    public static DepartmentStaffResponse fromEntity(User user) {
        if (user == null) return null;
        DepartmentStaffResponse res = new DepartmentStaffResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setRole(user.getRole());
        if (user.getDepartment() != null) {
            res.setDepartmentId(user.getDepartment().getId());
            res.setDepartmentName(user.getDepartment().getName());
            res.setDepartmentCode(user.getDepartment().getCode());
        }
        res.setActive(user.isActive());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
