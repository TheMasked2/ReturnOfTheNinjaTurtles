package org.turtleshop.api.modules.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodModel {
    private Integer paymentMethodId;
    private String provider;
    private String type;
}