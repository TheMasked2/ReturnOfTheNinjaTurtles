package org.turtleshop.api.modules.category.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Represents the category domain model
public class CategoryModel {
    private int categoryId;
    private String name;
    private String description;
}
