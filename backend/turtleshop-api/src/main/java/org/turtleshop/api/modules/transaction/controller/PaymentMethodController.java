package org.turtleshop.api.modules.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.transaction.dto.PaymentMethodCreateRequest;
import org.turtleshop.api.modules.transaction.dto.PaymentMethodResponse;
import org.turtleshop.api.modules.transaction.model.PaymentMethodModel;
import org.turtleshop.api.modules.transaction.service.PaymentMethodService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_READ_ALL')")
    public ResponseEntity<List<PaymentMethodResponse>> listPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.listPaymentMethods().stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{paymentMethodId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ_ALL')")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethodById(@PathVariable int paymentMethodId) {
        return ResponseEntity.ok(toPaymentMethodResponse(paymentMethodService.getPaymentMethodById(paymentMethodId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_UPDATE_ALL')")
    public ResponseEntity<Void> createPaymentMethod(@RequestBody PaymentMethodCreateRequest request) {
        paymentMethodService.createPaymentMethod(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private PaymentMethodResponse toPaymentMethodResponse(PaymentMethodModel model) {
        return PaymentMethodResponse.builder()
                .paymentMethodId(model.getPaymentMethodId())
                .provider(model.getProvider())
                .type(model.getType())
                .build();
    }
}