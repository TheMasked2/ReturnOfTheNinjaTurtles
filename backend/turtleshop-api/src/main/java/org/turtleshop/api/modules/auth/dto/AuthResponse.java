package org.turtleshop.api.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String type; // Usually "Bearer"
    private CustomerResponse customer;
}