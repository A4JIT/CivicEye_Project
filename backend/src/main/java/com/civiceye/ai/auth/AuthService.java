package com.civiceye.ai.auth;

import com.civiceye.ai.auth.dto.AuthResponse;
import com.civiceye.ai.auth.dto.LoginRequest;
import com.civiceye.ai.auth.dto.RegisterRequest;
import com.civiceye.ai.exception.BadRequestException;
import com.civiceye.ai.exception.UnauthorizedException;
import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CITIZEN;

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        String token = generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .expiresIn(86400000L)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail().trim();
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("Account has been disabled. Please contact administration.");
        }

        String token = generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .expiresIn(86400000L)
                .build();
    }

    private String generateToken(User user) {
        // Generates an authenticated session token for Phase 1
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                UUID.randomUUID().toString().replace("-", "") + "." +
                user.getId();
    }
}
