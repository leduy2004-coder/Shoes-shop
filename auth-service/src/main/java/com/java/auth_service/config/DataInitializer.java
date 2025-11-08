package com.java.auth_service.config;


import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.repository.RoleRepository;
import com.java.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor

public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:admin}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:admin}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.name:Administrator}")
    private String adminName;

    @Override
    public void run(String... args) {
        // 1) Seed roles
        String[] defaultRoles = {"ADMIN", "USER"};
        for (String roleName : defaultRoles) {
            roleRepository.findByCode(roleName)
                    .or(() -> Optional.of(roleRepository.save(new RoleEntity(roleName, roleName))))
                    .ifPresent(r -> System.out.println("Role ensured: " + r.getCode()));
        }

        // 2) Seed admin user nếu chưa có
        String email = adminEmail.trim().toLowerCase();
        if (userRepository.findByEmail(email).isEmpty()) {
            RoleEntity adminRole = roleRepository.findByCode("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

            UserEntity admin = UserEntity.builder()
                    .name(adminName)
                    .email(email)
                    .password(passwordEncoder.encode(adminPassword))
                    .status(true)
                    .role(adminRole)              // nếu bạn dùng DBRef/embedded thì vẫn set như thế này
                    .build();

            userRepository.save(admin);
            System.out.println("Inserted admin user: " + email);
        } else {
            System.out.println("Admin user already exists: " + email);
        }
    }
}
