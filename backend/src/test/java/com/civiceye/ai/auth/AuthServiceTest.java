package com.civiceye.ai.auth;

import com.civiceye.ai.auth.dto.AuthResponse;
import com.civiceye.ai.auth.dto.LoginRequest;
import com.civiceye.ai.auth.dto.RegisterRequest;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.UnauthorizedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.civiceye.ai.security.JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("$2a$10$encodedPasswordHash")
                .fullName("John Doe")
                .role(Role.CITIZEN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("john@example.com")
                .password("Password@123")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Password@123")
                .fullName("John Doe")
                .role(Role.CITIZEN)
                .build();

        lenient().when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("test-token");
        lenient().when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully login when valid email and password are provided")
    void login_Success_WhenValidEmailAndPassword() {
        when(userRepository.findByUsernameOrEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", sampleUser.getPassword())).thenReturn(true);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CITIZEN, response.getRole());
        verify(userRepository, times(1)).findByUsernameOrEmail("john@example.com");
        verify(passwordEncoder, times(1)).matches("Password@123", sampleUser.getPassword());
    }

    @Test
    @DisplayName("Should successfully login when valid username and password are provided")
    void login_Success_WhenValidUsernameAndPassword() {
        LoginRequest usernameLogin = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("Password@123")
                .build();

        when(userRepository.findByUsernameOrEmail("john_doe")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", sampleUser.getPassword())).thenReturn(true);

        AuthResponse response = authService.login(usernameLogin);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals(1L, response.getUserId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not found")
    void login_ThrowsUnauthorizedException_WhenUserNotFound() {
        when(userRepository.findByUsernameOrEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest unknownLogin = LoginRequest.builder()
                .usernameOrEmail("unknown@example.com")
                .password("Password@123")
                .build();

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(unknownLogin)
        );

        assertTrue(exception.getMessage().contains("Invalid username/email or password"));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password does not match")
    void login_ThrowsUnauthorizedException_WhenPasswordDoesNotMatch() {
        when(userRepository.findByUsernameOrEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", sampleUser.getPassword())).thenReturn(false);

        LoginRequest badPasswordLogin = LoginRequest.builder()
                .usernameOrEmail("john@example.com")
                .password("WrongPassword")
                .build();

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(badPasswordLogin)
        );

        assertTrue(exception.getMessage().contains("Invalid username/email or password"));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user account is deactivated")
    void login_ThrowsUnauthorizedException_WhenAccountDeactivated() {
        sampleUser.setActive(false);
        when(userRepository.findByUsernameOrEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", sampleUser.getPassword())).thenReturn(true);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("disabled"));
    }

    // ==========================================
    // REGISTER TESTS
    // ==========================================

    @Test
    @DisplayName("Should successfully register user and return AuthResponse")
    void register_Success_WhenValidRequest() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CITIZEN, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should default role to CITIZEN when role is omitted in registration")
    void register_Success_DefaultsToCitizen_WhenRoleIsNull() {
        registerRequest.setRole(null);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(Role.CITIZEN, response.getRole());
    }

    @Test
    @DisplayName("Should throw BadRequestException when email already exists")
    void register_ThrowsBadRequestException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when username already exists")
    void register_ThrowsBadRequestException_WhenUsernameAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("Username is already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when attempting to register non-citizen role publicly")
    void register_ThrowsBadRequestException_WhenRoleIsNotCitizen() {
        registerRequest.setRole(Role.ADMIN);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("restricted to citizens only"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully register admin when valid secret code is provided")
    void registerAdmin_Success_WhenValidCode() {
        authService.setAdminSecretCode("CIVICEYE-ADMIN-2026-SECURE");

        com.civiceye.ai.auth.dto.AdminRegisterRequest adminReq = com.civiceye.ai.auth.dto.AdminRegisterRequest.builder()
                .username("super_admin")
                .email("admin@civiceye.com")
                .password("Admin@123")
                .fullName("System Admin")
                .adminCode("CIVICEYE-ADMIN-2026-SECURE")
                .build();

        when(userRepository.existsByEmail("admin@civiceye.com")).thenReturn(false);
        when(userRepository.existsByUsername("super_admin")).thenReturn(false);
        when(passwordEncoder.encode("Admin@123")).thenReturn("$2a$10$encodedAdminHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        AuthResponse response = authService.registerAdmin(adminReq);

        assertNotNull(response);
        assertEquals("super_admin", response.getUsername());
        assertEquals("admin@civiceye.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when admin secret code is incorrect")
    void registerAdmin_ThrowsUnauthorizedException_WhenCodeIsInvalid() {
        authService.setAdminSecretCode("CIVICEYE-ADMIN-2026-SECURE");

        com.civiceye.ai.auth.dto.AdminRegisterRequest adminReq = com.civiceye.ai.auth.dto.AdminRegisterRequest.builder()
                .username("hacker")
                .email("hacker@civiceye.com")
                .password("Hacker@123")
                .fullName("Fake Admin")
                .adminCode("WRONG_CODE_XYZ")
                .build();

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.registerAdmin(adminReq)
        );

        assertTrue(exception.getMessage().contains("Invalid admin authorization code"));
        verify(userRepository, never()).save(any());
    }
}
