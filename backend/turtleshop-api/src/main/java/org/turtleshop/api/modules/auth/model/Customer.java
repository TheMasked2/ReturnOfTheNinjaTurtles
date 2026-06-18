package org.turtleshop.api.modules.auth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Customer {

    private UUID customerId;
    private String email;

    /*
     * The Java field can stay named password to keep changes small.
     * It contains the encoded hash and maps to password_hash in PostgreSQL.
     */
    private String password;

    private String firstName;
    private String lastName;

    /*
     * Sensitive customer information is stored separately.
     */
    private CustomerSensitiveData sensitiveData;

    private String bank;
    private LocalDateTime createdAt;
    private List<String> roles;
}