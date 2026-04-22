package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerAccess customerAccess;
    private final SystemRoleAccess roleAccess;

    // Get Customer by ID
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerAccess.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        return mapToResponse(customer);
    }

    // Get all Customers
    public List<CustomerResponse> getAllCustomers() {
        return customerAccess.getAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // HELPER: Maps the Model to the Response DTO
    private CustomerResponse mapToResponse(Customer customer) {
        List<String> roles = roleAccess.findRoleNamesByCustomerEmail(customer.getEmail());

        return CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .roles(roles)
                .build();
    }
}