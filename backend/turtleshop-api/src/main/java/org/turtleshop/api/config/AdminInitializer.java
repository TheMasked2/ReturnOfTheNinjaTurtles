package org.turtleshop.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final CustomerAccess customerAccess;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    // Automatically when application is being runned it will insert admin account.
    @Override
    public void run(String... args) {
        if (!customerAccess.existsByEmail(adminEmail)) {
            System.out.println("Creating default admin user: " + adminEmail);

            Customer admin = Customer.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("System")
                    .lastName("Admin")
                    .build();

            UUID adminId = customerAccess.insertAndReturnId(admin);

            customerAccess.addRoleToCustomer(adminId, "ROLE_ADMIN");
            customerAccess.addRoleToCustomer(adminId, "ROLE_USER");

            System.out.println("Admin user initialized successfully with UUID: " + adminId);
        }
    }
}