package org.turtleshop.api.modules.product.dto;

import lombok.Getter;
import lombok.Setter;
import org.turtleshop.api.modules.product.model.ProductModel;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;

    public ProductResponse(ProductModel product) {
        this.id = product.getProduct_id();
        this.name = product.getProduct_name();
        this.description = product.getDescription();
        this.price = product.getBase_price();
    }
}