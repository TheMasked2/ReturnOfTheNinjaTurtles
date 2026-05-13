package org.turtleshop.api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionUpdateRequest {
    private BigDecimal amount;
    private String status;
    private Integer paymentMethodId;
}