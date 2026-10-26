package com.civiceye.ai.auth;

import com.civiceye.ai.auth.dto.AuthResponse;
import com.civiceye.ai.auth.dto.LoginRequest;
import com.civiceye.ai.auth.dto.RegisterRequest;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.GlobalExceptionHandler;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse sampleAuthResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleAuthResponse = AuthResponse.builder()
                .token("test-jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .username("john_doe")
                .email("john@example.com")
                .fullName("John Doe")
                .role(Role.CITIZEN)
                .expiresIn(86400000L)
                .build();
    }

    // ==========================================
    // LOGIN CONTROLLER TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/auth/login should return HTTP 200 OK on valid credentials")
    void login_Returns200_WhenCredentialsValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john@example.com")
                .password("Password@123")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("john_doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return HTTP 401 Unauthorized on invalid credentials")
    void login_Returns401_WhenCredentialsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john@example.com")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid username/email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return HTTP 400 Bad Request when fields are missing")
    void login_Returns400_WhenFieldsMissing() throws Exception {
        LoginRequest badRequest = LoginRequest.builder()
                .usernameOrEmail("")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==========================================
    // REGISTER CONTROLLER TESTS
    // ==========================================

    @Test
    @DisplayName("POST /api/auth/register should return HTTP 201 Created on valid request")
    void register_Returns201_WhenValidData() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Password@123")
                .fullName("John Doe")
                .role(Role.CITIZEN)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john_doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return HTTP 400 Bad Request when email already exists")
    void register_Returns400_WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Password@123")
                .fullName("John Doe")
                .role(Role.CITIZEN)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email is already registered: john@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already registered: john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return HTTP 400 Bad Request when validation fails")
    void register_Returns400_WhenValidationFails() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("j") // too short
                .email("not-an-email") // invalid email
                .password("123") // too short
                .fullName("") // blank
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register-admin should return HTTP 201 Created when admin registration is successful")
    void registerAdmin_Returns201_WhenValid() throws Exception {
        com.civiceye.ai.auth.dto.AdminRegisterRequest request = com.civiceye.ai.auth.dto.AdminRegisterRequest.builder()
                .username("super_admin")
                .email("admin@civiceye.com")
                .password("Password@123")
                .fullName("Super Admin")
                .adminCode("CIVICEYE-ADMIN-2026-SECURE")
                .build();

        AuthResponse adminResponse = AuthResponse.builder()
                .token("admin-jwt-token")
                .tokenType("Bearer")
                .userId(10L)
                .username("super_admin")
                .email("admin@civiceye.com")
                .fullName("Super Admin")
                .role(Role.ADMIN)
                .expiresIn(86400000L)
                .build();

        when(authService.registerAdmin(any(com.civiceye.ai.auth.dto.AdminRegisterRequest.class)))
                .thenReturn(adminResponse);

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("super_admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/register-admin should return HTTP 401 when code is invalid")
    void registerAdmin_Returns401_WhenCodeInvalid() throws Exception {
        com.civiceye.ai.auth.dto.AdminRegisterRequest request = com.civiceye.ai.auth.dto.AdminRegisterRequest.builder()
                .username("super_admin")
                .email("admin@civiceye.com")
                .password("Password@123")
                .fullName("Super Admin")
                .adminCode("WRONG_CODE")
                .build();

        when(authService.registerAdmin(any(com.civiceye.ai.auth.dto.AdminRegisterRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid admin authorization code. Admin account creation is restricted."));

        mockMvc.perform(post("/api/auth/register-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid admin authorization code. Admin account creation is restricted."));
    }

    // ==========================================
    // HEALTH CHECK TEST
    // ==========================================

    @Test
    @DisplayName("GET /api/auth/health should return HTTP 200 OK")
    void healthCheck_Returns200() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("UP"));
    }
}
