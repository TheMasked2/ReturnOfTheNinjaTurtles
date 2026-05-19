package org.turtleshop.api.modules.productcategory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductCategoryRequest {
    private int productId;
    private int categoryId;
}
