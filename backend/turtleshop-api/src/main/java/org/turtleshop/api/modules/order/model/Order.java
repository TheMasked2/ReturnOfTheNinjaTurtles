package org.turtleshop.api.modules.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.turtleshop.api.modules.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Order {
    private int orderId;
    private UUID customerId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
}
