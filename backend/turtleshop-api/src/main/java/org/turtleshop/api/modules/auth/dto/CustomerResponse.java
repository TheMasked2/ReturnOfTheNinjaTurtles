package org.turtleshop.api.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private List<String> roles;
    private LocalDateTime createdAt;
}