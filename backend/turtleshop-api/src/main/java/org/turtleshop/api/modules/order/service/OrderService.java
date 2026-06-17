package org.turtleshop.api.modules.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.order.dto.OrderItemResponse;
import org.turtleshop.api.modules.order.dto.OrderResponse;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.model.Order;
import org.turtleshop.api.modules.order.model.OrderItem;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;
import org.turtleshop.api.modules.order.dto.OrderSummaryResponse;
import org.turtleshop.api.modules.order.model.OrderSummary;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderAccess orderAccess;
    private final OrderItemAccess orderItemAccess;

    // Get Order for an OrderId
    public OrderResponse getOrderByOrderId(int orderId) {
        Order existingOrder = orderAccess.getOrderById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No orders exist for this order id"));
        return mapToOrderResponse(existingOrder);
    }

    // Get all Orders for a customer
    public List<OrderResponse> getAllOrdersOfCustomer(UUID customerId) {
        List<Order> orders = orderAccess.getAllOrdersById(customerId);
        if (orders == null) {
            return List.of();
        }
        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    // Cancel Order based on OrderId
    public void cancelOrderByOrderId(int orderId) {
        Order order = orderAccess.getOrderById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No order exists for this OrderId"));
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is already completed or cancelled, it cant be cancelled anymore");
        }
        orderAccess.updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    // Get OrderItem by OrderItemId
    public OrderItemResponse getOrderItemById(int orderItemId) {
        OrderItem orderItem = orderItemAccess.getOrderItemById(orderItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "OrderItem does not exist for this OrderItemId"));
        return mapToOrderItemResponse(orderItem);
    }

    // Get all OrderItems in an Order
    public List<OrderItemResponse> getAllOrderItemsInOrder(int orderId) {
        List<OrderItem> orderItems = orderItemAccess.getAllOrderItems(orderId);
        if(orderItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No OrderItems found for this Order");
        }
        return orderItems.stream().map(this:: mapToOrderItemResponse).toList();
    }

    public List<OrderSummaryResponse> getOrderSummaries(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);

        List<OrderSummary> summaries = orderAccess.getOrderSummaries(safeLimit);

        return summaries.stream()
                .map(this::mapToOrderSummaryResponse)
                .toList();
    }

    // HELPER: Maps the Model to the Response DTO
    public OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .build();
    }

    // HELPER: Maps the Model to the Response DTO
    public OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrderId())
                .productId(orderItem.getProductId())
                .quantity(orderItem.getQuantity())
                .build();
    }

    // HELPER: Maps the Model to the Response DTO
    public OrderSummaryResponse mapToOrderSummaryResponse(OrderSummary orderSummary) {
        return OrderSummaryResponse.builder()
                .orderId(orderSummary.getOrderId())
                .customerId(orderSummary.getCustomerId())
                .customerEmail(orderSummary.getCustomerEmail())
                .orderDate(orderSummary.getOrderDate())
                .status(orderSummary.getStatus())
                .totalAmount(orderSummary.getTotalAmount())
                .itemLines(orderSummary.getItemLines())
                .totalItems(orderSummary.getTotalItems())
                .build();
    }
}
