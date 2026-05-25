package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
    public AuthResponse register(RegisterRequest request) {
        if (customerAccess.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        UUID customerId = customerAccess.insertAndReturnId(customer);
        customerAccess.addRoleToCustomer(customerId, "ROLE_USER");

        List<String> roles = List.of("ROLE_USER");
        String token = jwtService.generateToken(customer.getEmail(), roles);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .customer(CustomerResponse.builder()
                        .id(customerId)
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .roles(roles)
                        .build())
                .build();
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        Customer customer = customerAccess.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        List<String> roles = roleAccess.findRoleNamesByCustomerEmail(customer.getEmail());
        List<String> permissions = roleAccess.findPermissionCodesByCustomerEmail(customer.getEmail());

        String token = jwtService.generateToken(customer.getEmail(), roles, permissions);

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