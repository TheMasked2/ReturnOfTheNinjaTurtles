package org.turtleshop.api.modules.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
}
