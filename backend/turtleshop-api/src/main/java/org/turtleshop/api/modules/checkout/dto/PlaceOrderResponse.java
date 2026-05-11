package org.turtleshop.api.modules.checkout.dto;

import java.math.BigDecimal;

public class PlaceOrderResponse {
    private Integer orderId;
    private String orderStatus;
    private Integer transactionId;
    private String transactionStatus;
    private BigDecimal totalAmount;
}
