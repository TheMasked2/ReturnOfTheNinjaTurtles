package org.turtleshop.api.modules.recommendation.dto;

import java.math.BigDecimal;

public record RecommendedProduct(
    int productId,
    String productName,
    BigDecimal price,
    String imageUrl
) {}