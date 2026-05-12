package org.turtleshop.api.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Integer transactionId;
    private Integer orderId;
    private Integer paymentMethodId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime transactionDate;
}