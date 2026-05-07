package org.turtleshop.api.modules.cart.model;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.time.LocalDateTime;


@Getter
@Setter
@Build
public class Cart {
    private int cartId;
    private UUID customerId;
    private int orderId;
    private CartStatus status;
    private LocalDateTime createdAt;
}