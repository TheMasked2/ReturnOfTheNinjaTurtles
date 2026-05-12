package org.turtleshop.api.modules.order.dto;

import lombok.Builder;
import lombok.Getter;
import org.turtleshop.api.modules.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {
    private int orderId;
    private UUID customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
}
