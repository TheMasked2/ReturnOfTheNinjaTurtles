package org.turtleshop.api.modules.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ORDER_READ_ALL') or " +
            "(hasAuthority('ORDER_READ_OWN') and @authorizationService.isOrderOwner(#orderId, authentication))")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getOrderByOrderId(orderId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('ORDER_READ_ALL') or " +
            "(hasAuthority('ORDER_READ_OWN') and @authorizationService.isCurrentCustomer(#customerId, authentication))")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.getAllOrdersOfCustomer(customerId));
    }

    @GetMapping("/items/{orderItemId}")
    @PreAuthorize("hasAuthority('ORDER_READ_ALL') or " +
            "(hasAuthority('ORDER_READ_OWN') and @authorizationService.isOrderItemOwner(#orderItemId, authentication))")
    public ResponseEntity<OrderItemResponse> getOrderItem(@PathVariable int orderItemId) {
        return ResponseEntity.ok(orderService.getOrderItemById(orderItemId));
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasAuthority('ORDER_READ_ALL') or " +
            "(hasAuthority('ORDER_READ_OWN') and @authorizationService.isOrderOwner(#orderId, authentication))")
    public ResponseEntity<List<OrderItemResponse>> getAllOrderItems(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getAllOrderItemsInOrder(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('ORDER_UPDATE_ALL') or " +
            "(hasAuthority('ORDER_UPDATE_OWN') and @authorizationService.isOrderOwner(#orderId, authentication))")
    public ResponseEntity<String> cancelOrder(@PathVariable int orderId) {
        orderService.cancelOrderByOrderId(orderId);
        return ResponseEntity.ok("Order is cancelled");
    }
}