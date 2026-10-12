package com.civiceye.ai.security;

import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;
    private final String secret = "super_secret_test_key_for_jwt_unit_tests_1234567890";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);

        testUser = User.builder()
                .id(42L)
                .username("testcitizen")
                .email("citizen@civiceye.org")
                .password("encoded_pass")
                .fullName("Test Citizen")
                .role(Role.CITIZEN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should generate non-null and valid JWT token")
    void generateToken_Success() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should extract correct username from token")
    void getUsernameFromToken_Success() {
        String token = jwtTokenProvider.generateToken(testUser);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testcitizen", username);
    }

    @Test
    @DisplayName("Should extract correct claims (userId, role) from token")
    void extractClaims_Success() {
        String token = jwtTokenProvider.generateToken(testUser);

        assertEquals(42L, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("CITIZEN", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should return false for invalid / malformed token")
    void validateToken_ReturnsFalse_WhenTokenMalformed() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should return false for expired token")
    void validateToken_ReturnsFalse_WhenTokenExpired() throws InterruptedException {
        // Token with 1ms expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1);
        String token = shortLivedProvider.generateToken(testUser);

        Thread.sleep(10); // Wait for expiry
        assertFalse(shortLivedProvider.validateToken(token));
    }
}
