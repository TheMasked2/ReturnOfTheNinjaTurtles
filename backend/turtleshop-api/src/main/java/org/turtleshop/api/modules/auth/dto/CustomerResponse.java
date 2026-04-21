package org.turtleshop.api.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

// jakarta validation
// voorbeeld:
// @NotBlank(message = "Email cannot be empty")
// @Size(min = 2, message = "Email must at least be 2 characters")

@Getter
@Builder
public class CustomerResponse {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime createdAt;
}