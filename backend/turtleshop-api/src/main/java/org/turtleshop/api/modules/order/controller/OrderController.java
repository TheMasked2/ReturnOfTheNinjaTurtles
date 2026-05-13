package org.turtleshop.api.modules.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.order.dto.OrderItemResponse;
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
    public ResponseEntity<List<OrderResponse>> getAllOrders(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.getAllOrdersOfCustomer(customerId));
    }

    @GetMapping("/items/{orderItemId}")
    public ResponseEntity<OrderItemResponse> getOrderItem(@PathVariable int orderItemId) {
        return ResponseEntity.ok(orderService.getOrderItemById(orderItemId));
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getAllOrderItemsInOrder(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable int orderId) {
        orderService.cancelOrderByOrderId(orderId);
        return ResponseEntity.ok("Order is cancelled");
    }
}
