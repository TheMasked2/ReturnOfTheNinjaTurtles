package org.turtleshop.api.modules.order.enums;

public enum OrderStatus {
    CONFIRMED, // in payment/transaction after succesfull
    AWAITING_PAYMENT, // in checkout when placing order
    CANCELLED, // in orderservice
    COMPLETED // after delivery mark completed
}
