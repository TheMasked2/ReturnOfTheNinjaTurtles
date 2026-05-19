package org.turtleshop.api.modules.category.dto;

import org.turtleshop.api.modules.category.model.CategoryModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Response DTO for category output
public class CategoryResponse {
    private int id;
    private String name;
    private String description;

    // Build response from category model
    public CategoryResponse(CategoryModel category) {
        this.id = category.getCategoryId();
        this.name = category.getName();
        this.description = category.getDescription();
    }
}
