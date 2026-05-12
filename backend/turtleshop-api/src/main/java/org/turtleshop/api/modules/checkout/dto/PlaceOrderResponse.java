package org.turtleshop.api.modules.checkout.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PlaceOrderResponse {
    private Integer orderId;
    private String orderStatus;
    private Integer shipmentId;
    private Integer transactionId;
    private String transactionStatus;
    private BigDecimal totalAmount;
}