package org.turtleshop.api.modules.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String specs;
    private LocalDate availableSince;
    private List<String> suggestedProducts;
}