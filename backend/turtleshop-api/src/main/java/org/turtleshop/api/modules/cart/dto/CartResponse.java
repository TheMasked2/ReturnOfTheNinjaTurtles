package org.turtleshop.api.modules.cart.dto;

import lombok.Builder;
import lombok.Getter;
import org.turtleshop.api.modules.cart.enums.CartStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartResponse {
    private int cartId;
    private UUID customerId;
    private Integer orderId;
    private CartStatus status;
    private LocalDateTime createdAt;
    private List<CartItemResponse> items;

    public static class PlaceOrderResponse {
        private Integer orderId;
        private String orderStatus;
        private Integer transactionId;
        private String transactionStatus;
        private BigDecimal totalAmount;
    }
}
