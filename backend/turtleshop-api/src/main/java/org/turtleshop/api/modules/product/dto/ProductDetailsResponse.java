package org.turtleshop.api.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // Relational components sourced from PostgreSQL
    private int productId;
    private String productName;
    private BigDecimal basePrice;
    
    // Unstructured components sourced from MongoDB
    private String sku;
    private String description;
    private Map<String, Object> specs;
    private List<String> suggestedProducts;
}