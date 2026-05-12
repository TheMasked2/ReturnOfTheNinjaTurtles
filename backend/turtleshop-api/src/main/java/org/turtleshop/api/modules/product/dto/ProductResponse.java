package org.turtleshop.api.modules.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.turtleshop.api.modules.product.model.ProductModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponse {
    private int id;
    private String name;
    private String description;
    private String specs;
    private BigDecimal price;
    private LocalDate availableSince;
    private List<String> suggestedProducts;

    public ProductResponse(ProductModel product) {
        this.id = product.getProductId();
        this.name = product.getProductName();
        this.description = product.getDescription();
        this.specs = product.getSpecs();
        this.price = product.getBasePrice();
        this.availableSince = product.getAvailableSince();
        this.suggestedProducts = product.getSuggestedProducts();
    }
}