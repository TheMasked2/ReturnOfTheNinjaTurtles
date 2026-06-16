package org.turtleshop.api.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.auth.dto.AuthResponse;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.dto.LoginRequest;
import org.turtleshop.api.modules.auth.dto.RegisterRequest;
import org.turtleshop.api.modules.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Logout
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    // @PostMapping("/refresh")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<AuthResponse> refreshToken() {
    //     return ResponseEntity.ok(authService.refreshToken());
    // }

     // Get current user info
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public CustomerResponse getCurrentUser(Authentication authentication) {
         return authService.getCurrentCustomer(authentication.getName());
    }
}