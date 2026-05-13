package org.turtleshop.api.modules.cart.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
}