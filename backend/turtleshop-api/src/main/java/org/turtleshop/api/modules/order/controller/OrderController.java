package org.turtleshop.api.modules.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.order.dto.OrderResponse;
import org.turtleshop.api.modules.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getOrderByOrderId(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getAllOrders(UUID customerId) {
        return ResponseEntity.ok(orderService.getAllOrdersOfCustomer(customerId));
    }
}
