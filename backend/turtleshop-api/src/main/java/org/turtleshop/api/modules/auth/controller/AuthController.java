package org.turtleshop.api.modules.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.turtleshop.api.modules.auth.dto.*;
import org.turtleshop.api.modules.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registration successful");
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