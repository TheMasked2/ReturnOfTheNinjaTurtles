package org.turtleshop.api.modules.product.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Product {
    private int product_id;
    private BigDecimal base_price;
    private String product_name;
    private String description;
    private String specs;
    private LocalDate available_name;
}