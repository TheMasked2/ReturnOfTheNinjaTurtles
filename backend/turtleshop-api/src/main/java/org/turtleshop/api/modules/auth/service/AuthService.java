package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.turtleshop.api.modules.auth.dto.*;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerAccess customerAccess;
    private final SystemRoleAccess roleAccess;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Register
    @Transactional
    public void register(RegisterRequest request) {
        if (customerAccess.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Customer customer = Customer.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();

        UUID newId = customerAccess.insertAndReturnId(customer);

        customerAccess.addRoleToCustomer(newId, "ROLE_USER");
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        Customer customer = customerAccess.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        List<String> roles = roleAccess.findRoleNamesByCustomerEmail(customer.getEmail());
        String token = jwtService.generateToken(customer.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .customer(CustomerResponse.builder()
                        .id(customer.getCustomerId())
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .roles(roles)
                        .build())
                .build();
    }
}