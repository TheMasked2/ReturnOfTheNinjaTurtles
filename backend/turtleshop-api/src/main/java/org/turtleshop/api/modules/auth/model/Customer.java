package org.turtleshop.api.modules.auth.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;  // Add this
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor  // Required for many mappers
@AllArgsConstructor
public class Customer {
    private Integer customerId;
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
}