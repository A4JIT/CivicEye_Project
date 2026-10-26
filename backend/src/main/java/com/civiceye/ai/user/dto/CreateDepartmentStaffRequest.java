package com.civiceye.ai.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentStaffRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public static CreateDepartmentStaffRequestBuilder builder() {
        return new CreateDepartmentStaffRequestBuilder();
    }

    public static class CreateDepartmentStaffRequestBuilder {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private Long departmentId;

        public CreateDepartmentStaffRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CreateDepartmentStaffRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CreateDepartmentStaffRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public CreateDepartmentStaffRequestBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public CreateDepartmentStaffRequestBuilder departmentId(Long departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public CreateDepartmentStaffRequest build() {
            CreateDepartmentStaffRequest req = new CreateDepartmentStaffRequest();
            req.setUsername(this.username);
            req.setEmail(this.email);
            req.setPassword(this.password);
            req.setFullName(this.fullName);
            req.setDepartmentId(this.departmentId);
            return req;
        }
    }
}
