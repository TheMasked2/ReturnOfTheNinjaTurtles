package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.auth.dto.*;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerAccess customerAccess;
    private final BCryptPasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        // Hash password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();

        customerAccess.insertAsync(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerAccess.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Mapping to Response DTO
        CustomerResponse customerDto = CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .build();

        return AuthResponse.builder()
                .token("mock-jwt-token-for-" + customer.getEmail())
                .type("Bearer")
                .customer(customerDto)
                .build();
    }
}
