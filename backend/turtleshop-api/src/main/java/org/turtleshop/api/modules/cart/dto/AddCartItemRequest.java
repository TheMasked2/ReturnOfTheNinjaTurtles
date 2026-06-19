package org.turtleshop.api.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {

    @NotNull
    @Min(1)
    private int productId;

    @NotNull
    @Min(1)
    private int quantity;
}
