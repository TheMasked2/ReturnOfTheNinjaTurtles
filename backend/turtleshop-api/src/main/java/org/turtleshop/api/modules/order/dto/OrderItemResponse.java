package org.turtleshop.api.modules.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
}
