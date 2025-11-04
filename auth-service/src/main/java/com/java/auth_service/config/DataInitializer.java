package com.java.auth_service.config;


import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInitializer implements CommandLineRunner {

    RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        String[] defaultRoles = {"ADMIN", "USER", "MANAGER"};

        for (String roleName : defaultRoles) {
            if (roleRepository.findByCode(roleName).isEmpty()) {
                roleRepository.save(new RoleEntity(roleName, roleName));
                System.out.println("Inserted role: " + roleName);
            }
        }
    }
}
