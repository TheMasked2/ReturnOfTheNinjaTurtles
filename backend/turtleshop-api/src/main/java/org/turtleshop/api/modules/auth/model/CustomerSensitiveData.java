package org.turtleshop.api.modules.auth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CustomerSensitiveData {

    private UUID customerId;
    private String phone;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private LocalDateTime updatedAt;
}