package org.turtleshop.api.modules.category.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Request DTO for updating categories
public class UpdateCategoryRequest {
    private String name;
    private String description;
}
