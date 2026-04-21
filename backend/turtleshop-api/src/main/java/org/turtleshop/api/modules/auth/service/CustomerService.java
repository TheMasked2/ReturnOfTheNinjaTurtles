package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically creates constructor for the final CustomerAccess
public class CustomerService {

    private final CustomerAccess customerAccess;

    /**
     * Finds a customer and converts it to a Response DTO.
     * Uses the getByIdAsync method from your GenericAccess.
     */
    public CustomerResponse getCustomerById(Integer id) {
        return customerAccess.getByIdAsync(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Customer with ID " + id + " not found"));
    }

    /**
     * Retrieves all customers and maps them to a list of DTOs.
     */
    public List<CustomerResponse> getAllCustomers() {
        return customerAccess.getAllAsync().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Handles the creation of a new customer.
     */
    public void createCustomer(Customer customer) {
        // You can add business logic here (e.g., checking if email exists)
        customerAccess.insertAsync(customer);
    }

    /**
     * Helper method to map our Database Model to our API Response DTO.
     * This ensures we don't expose sensitive data like passwords to the frontend.
     */
    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}