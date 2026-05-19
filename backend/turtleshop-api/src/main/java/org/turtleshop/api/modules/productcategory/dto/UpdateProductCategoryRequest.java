package org.turtleshop.api.modules.productcategory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductCategoryRequest {
    private Integer newProductId;
    private Integer newCategoryId;
}
