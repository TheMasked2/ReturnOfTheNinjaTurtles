package org.turtleshop.api.modules.category.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Request DTO for creating categories
public class CreateCategoryRequest {
    private String name;
    private String description;
}
