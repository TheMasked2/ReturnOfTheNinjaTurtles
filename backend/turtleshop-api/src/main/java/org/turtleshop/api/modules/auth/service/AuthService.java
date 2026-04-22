package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.dto.*;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerAccess customerAccess;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        customerAccess.insert(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerAccess.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String realToken = jwtService.generateToken(customer.getEmail());

        // Mapping to Response DTO
        CustomerResponse customerDto = CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .build();

        return AuthResponse.builder()
                .token(realToken)
                .type("Bearer")
                .customer(customerDto)
                .build();
    }
}
