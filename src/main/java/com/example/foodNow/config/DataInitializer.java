package com.example.foodNow.config;

import com.example.foodNow.model.User;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("No users found. Creating default ADMIN user...");

                User admin = new User();
                admin.setEmail("admin@test.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("Super Admin");
                admin.setPhoneNumber("0000000000");
                admin.setRole(User.Role.ADMIN);
                admin.setIsActive(true);

                userRepository.save(admin);

                log.info("========================================");
                log.info("Default ADMIN user created!");
                log.info("Email: admin@test.com");
                log.info("Password: admin123");
                log.info("========================================");
            } else {
                // Check if specifically admin@test.com exists, if not maybe create it?
                // For now, if users exist, we assume admin might be there or we don't want to
                // mess up.
                // But better to ensure at least one admin exists.
                if (userRepository.findByEmail("admin@test.com").isEmpty()) {
                    User admin = new User();
                    admin.setEmail("admin@test.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setFullName("Super Admin");
                    admin.setPhoneNumber("0000000000");
                    admin.setRole(User.Role.ADMIN);
                    admin.setIsActive(true);
                    userRepository.save(admin);
                    log.info("Default ADMIN user (admin@test.com) created.");
                }
            }
        };
    }
}
