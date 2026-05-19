package org.turtleshop.api.modules.productcategory.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Model for the product-category join table
public class ProductCategoryModel {
    private int productId;
    private int categoryId;
}
