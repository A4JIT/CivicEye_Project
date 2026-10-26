package com.civiceye.ai.security;

import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .username("secuser")
                .email("sec@civiceye.org")
                .password(passwordEncoder.encode("SecretPass123"))
                .fullName("Security User")
                .role(Role.CITIZEN)
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Unknown routes without auth should return 401 Unauthorized")
    void unknownRoute_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/unknown-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth endpoints should be publicly accessible")
    void authEndpoints_ArePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint with valid JWT Bearer token should be accessible")
    void protectedEndpoint_WithValidJwt_IsAccessible() throws Exception {
        String token = jwtTokenProvider.generateToken(savedUser);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected endpoint with invalid JWT should return 401 Unauthorized")
    void protectedEndpoint_WithInvalidJwt_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Department endpoints without auth should return 401 Unauthorized")
    void departmentEndpoint_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/department/complaints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Department endpoints with citizen role should return 403 Forbidden")
    void departmentEndpoint_WithCitizenRole_Returns403() throws Exception {
        String token = jwtTokenProvider.generateToken(savedUser);

        mockMvc.perform(get("/api/department/complaints")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
