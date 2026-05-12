package org.turtleshop.api.modules.checkout.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {
    private String shippingMethod;
    private String shippingAddress;
    private String paymentMethod;
}
