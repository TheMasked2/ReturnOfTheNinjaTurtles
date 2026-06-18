package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // Get Customers paginated
    public Page<CustomerResponse> getCustomersPage(int page, int size) {
        int safePage = Math.max(page, 0);

        // Minimum size = 1, maximum size = 100
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("createdAt").descending()
        );

        return customerAccess.getPage(pageable)
                .map(this::mapToResponse);
    }

    // Optional: but dangerous cuz returns all 100000 customers
    public List<CustomerResponse> getAllCustomers() {
        return customerAccess.getAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // HELPER: Maps the Model to the Response DTO
    private CustomerResponse mapToResponse(Customer customer) {
        List<String> roles =
                roleAccess.findRoleNamesByCustomerEmail(
                        customer.getEmail()
                );

        return CustomerResponse.builder()
                .id(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(
                        customer.getSensitiveData() == null
                                ? null
                                : customer.getSensitiveData().getPhone()
                )
                .createdAt(customer.getCreatedAt())
                .roles(roles)
                .build();
    }
}