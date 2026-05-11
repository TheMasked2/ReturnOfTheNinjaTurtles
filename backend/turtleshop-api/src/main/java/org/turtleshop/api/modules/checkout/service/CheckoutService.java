package org.turtleshop.api.modules.checkout.service;

import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.order.repository.OrderAccess;

import java.util.UUID;

public class CheckoutService {

    private CartAccess cartAccess;
    private OrderAccess orderAccess;

    public PlaceOrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
        // 1. Get active cart
        // 2. Check cart is not empty
        // 3. Calculate total amount
        // 4. Create order with status PENDING
        // 5. Create order items from cart items
        // 6. Create shipment if your design creates shipment immediately
        // 7. Create transaction with status PENDING
        // 8. Link cart to order, but keep cart status ACTIVE
        // 9. Return order/payment/shipment info
    }
}
