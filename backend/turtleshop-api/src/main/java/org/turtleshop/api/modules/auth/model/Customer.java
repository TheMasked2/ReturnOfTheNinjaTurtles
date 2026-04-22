package org.turtleshop.api.modules.auth.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Customer {
    private UUID customerId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String bank;
    private LocalDateTime createdAt;
    private List<String> roles;
}