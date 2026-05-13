package org.turtleshop.api.modules.checkout.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.checkout.service.CheckoutService;

import java.util.UUID;

@RestController
@RequestMapping("api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/customer/{customerId}/place-order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@PathVariable UUID customerId, @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(checkoutService.placeOrder(customerId, request));
    }
}