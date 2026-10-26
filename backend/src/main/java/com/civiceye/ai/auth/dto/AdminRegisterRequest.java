package com.civiceye.ai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterRequest {

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

    @NotBlank(message = "Special admin authorization code is required")
    private String adminCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public static AdminRegisterRequestBuilder builder() {
        return new AdminRegisterRequestBuilder();
    }

    public static class AdminRegisterRequestBuilder {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String adminCode;

        public AdminRegisterRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AdminRegisterRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AdminRegisterRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public AdminRegisterRequestBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public AdminRegisterRequestBuilder adminCode(String adminCode) {
            this.adminCode = adminCode;
            return this;
        }

        public AdminRegisterRequest build() {
            AdminRegisterRequest req = new AdminRegisterRequest();
            req.setUsername(this.username);
            req.setEmail(this.email);
            req.setPassword(this.password);
            req.setFullName(this.fullName);
            req.setAdminCode(this.adminCode);
            return req;
        }
    }
}
