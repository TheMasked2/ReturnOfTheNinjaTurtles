package org.turtleshop.api.modules.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.auth.dto.CustomerResponse;
import org.turtleshop.api.modules.auth.service.CustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Get customer by id
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_READ_ALL') or " +
            "(hasAuthority('CUSTOMER_READ_OWN') and @authorizationService.isCurrentCustomer(#id, authentication))")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // Get all customers
    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ_ALL')")
    public ResponseEntity<Page<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        return ResponseEntity.ok(customerService.getCustomersPage(safePage, safeSize));
    }
}