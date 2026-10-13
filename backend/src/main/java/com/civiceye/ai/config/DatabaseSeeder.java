package com.civiceye.ai.config;

import com.civiceye.ai.user.Role;
import com.civiceye.ai.user.User;
import com.civiceye.ai.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        syncUser("admin@civiceye.com", "admin", "CivicEye Administrator", Role.ADMIN);
        syncUser("citizen@civiceye.com", "citizen_jane", "Jane Citizen", Role.CITIZEN);
        syncUser("dept_roads@civiceye.com", "officer_sharma", "Rajesh Sharma (PWD Officer)", Role.DEPARTMENT_USER);
    }

    private void syncUser(String email, String username, String fullName, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if (!passwordEncoder.matches("Password@123", user.getPassword())) {
                user.setPassword(passwordEncoder.encode("Password@123"));
                userRepository.save(user);
                log.info("Updated password for test account: {}", email);
            }
        }, () -> {
            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .password(passwordEncoder.encode("Password@123"))
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(newUser);
            log.info("Created missing default test account: {}", email);
        });
    }
}
