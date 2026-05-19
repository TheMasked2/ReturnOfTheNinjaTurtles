package org.turtleshop.api.modules.productcategory.dto;

import org.turtleshop.api.modules.productcategory.model.ProductCategoryModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Response DTO for product-category mapping
public class ProductCategoryResponse {
    private int productId;
    private int categoryId;

    public ProductCategoryResponse(ProductCategoryModel productCategory) {
        this.productId = productCategory.getProductId();
        this.categoryId = productCategory.getCategoryId();
    }
}
