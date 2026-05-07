package org.turtleshop.api.modules.cart.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Build
public class CartItem {
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
}