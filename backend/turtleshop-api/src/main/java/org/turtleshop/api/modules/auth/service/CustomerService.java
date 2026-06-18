package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.dto.CustomerUpdateRequest;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerAccess customerAccess;
    private final SystemRoleAccess roleAccess;
    private final PasswordEncoder passwordEncoder;

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

    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer existingCustomer = customerAccess.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        try{
            validateUpdateRequest(request);
        }catch (ResponseStatusException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getReason());
        }

        String email = StringUtils.hasText(request.getEmail())
                ? request.getEmail().trim()
                : existingCustomer.getEmail();
    
        if (!email.equalsIgnoreCase(existingCustomer.getEmail()) && customerAccess.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use.");
        }
    
        String encodedPassword = existingCustomer.getPassword();
        if (StringUtils.hasText(request.getPassword())) {
            encodedPassword = passwordEncoder.encode(request.getPassword().trim());
        }
    
        Customer updatedCustomer = Customer.builder()
                .customerId(existingCustomer.getCustomerId())
                .email(email)
                .password(encodedPassword)
                .firstName(StringUtils.hasText(request.getFirstName())
                        ? request.getFirstName().trim()
                        : existingCustomer.getFirstName())
                .lastName(StringUtils.hasText(request.getLastName())
                        ? request.getLastName().trim()
                        : existingCustomer.getLastName())
                .phone(StringUtils.hasText(request.getPhone())
                        ? request.getPhone().trim()
                        : existingCustomer.getPhone())
                .address(request.getAddress() != null
                        ? request.getAddress().trim()
                        : existingCustomer.getAddress())
                .city(request.getCity() != null
                        ? request.getCity().trim()
                        : existingCustomer.getCity())
                .postalCode(request.getPostalCode() != null
                        ? request.getPostalCode().trim()
                        : existingCustomer.getPostalCode())
                .country(request.getCountry() != null
                        ? request.getCountry().trim()
                        : existingCustomer.getCountry())
                .createdAt(existingCustomer.getCreatedAt())
                .roles(existingCustomer.getRoles())
                .sensitiveData(existingCustomer.getSensitiveData())
                .build();
    
        customerAccess.updateCustomer(id, updatedCustomer);
    
        return mapToResponse(updatedCustomer);
    }
    
    private void validateUpdateRequest(CustomerUpdateRequest request) {
        if (StringUtils.hasText(request.getEmail()) &&
                !request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is invalid.");
        }
    
        if (StringUtils.hasText(request.getPassword()) && request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
        }
    
        if (StringUtils.hasText(request.getPhone()) &&
                !request.getPhone().matches("^[0-9+\\-() ]{7,25}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number contains invalid characters.");
        }
    
        if (request.getPostalCode() != null && request.getPostalCode().length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code is too long.");
        }
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
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .createdAt(customer.getCreatedAt())
                .roles(roles)
                .build();
    }
}