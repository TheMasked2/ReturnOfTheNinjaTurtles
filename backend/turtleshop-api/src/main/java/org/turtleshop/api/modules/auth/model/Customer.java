package org.turtleshop.api.modules.auth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@Builder
public class Customer {

    private UUID customerId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private CustomerSensitiveData sensitiveData;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime createdAt;
    private List<String> roles;
}