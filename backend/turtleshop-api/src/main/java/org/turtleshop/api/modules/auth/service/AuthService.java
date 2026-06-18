package org.turtleshop.api.modules.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.dto.AuthResponse;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.dto.LoginRequest;
import org.turtleshop.api.modules.auth.dto.RegisterRequest;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;
import org.turtleshop.api.modules.auth.model.CustomerSensitiveData;

import lombok.RequiredArgsConstructor;

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

        CustomerSensitiveData sensitiveData =
                CustomerSensitiveData.builder()
                        .phone(request.getPhone())
                        .build();

        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .sensitiveData(sensitiveData)
                .build();

        UUID customerId = customerAccess.insertAndReturnId(customer);
        customerAccess.addRoleToCustomer(customerId, "ROLE_USER");

        List<String> roles = List.of("ROLE_USER");

        // Fetch the permissions automatically assigned to the ROLE_USER
        List<String> permissions = roleAccess.findPermissionCodesByCustomerEmail(customer.getEmail());

        String token = jwtService.generateToken(customer.getEmail(), roles, permissions);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .customer(CustomerResponse.builder()
                        .id(customerId)
                        .email(customer.getEmail())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .phone(sensitiveData.getPhone())
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
                        .phone(getPhone(customer))
                        .roles(roles)
                        .build())
                .build();
    }

    // Get current user info
    public CustomerResponse getCurrentCustomer(String email) {
        Customer customer = customerAccess.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<String> roles = roleAccess.findRoleNamesByCustomerEmail(customer.getEmail());

        return CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(
                        customer.getSensitiveData() == null
                                ? null
                                : customer.getSensitiveData().getPhone()
                )
                .roles(roles)
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private String getPhone(Customer customer) {
        if (customer.getSensitiveData() == null) {
            return null;
        }

        return customer.getSensitiveData().getPhone();
    }
}