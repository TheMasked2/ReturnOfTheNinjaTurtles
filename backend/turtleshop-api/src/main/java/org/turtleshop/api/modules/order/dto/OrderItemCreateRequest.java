package org.turtleshop.api.modules.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemCreateRequest {
    @NotNull
    @Min(1)
    private int productId;

    @NotNull
    @Min(1)
    private int quantity;
}
