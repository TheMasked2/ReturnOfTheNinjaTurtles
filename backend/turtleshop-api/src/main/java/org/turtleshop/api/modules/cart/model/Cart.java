package org.turtleshop.api.modules.cart.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.turtleshop.api.modules.cart.enums.CartStatus;
import java.util.UUID;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class Cart {
    private int cartId;
    private UUID customerId;
    private Integer orderId;
    private CartStatus status;
    private LocalDateTime createdAt;
}