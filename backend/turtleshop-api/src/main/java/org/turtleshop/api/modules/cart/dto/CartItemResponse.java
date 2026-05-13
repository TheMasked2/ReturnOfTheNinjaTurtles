package org.turtleshop.api.modules.cart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
}
