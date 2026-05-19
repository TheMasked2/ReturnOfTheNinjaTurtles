package org.turtleshop.api.modules.category.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.category.dto.CreateCategoryRequest;
import org.turtleshop.api.modules.category.dto.UpdateCategoryRequest;
import org.turtleshop.api.modules.category.model.CategoryModel;
import org.turtleshop.api.modules.category.repository.CategoryAccess;

@Service
// Business logic for category operations
public class CategoryService {
    private final CategoryAccess categoryAccess;

    public CategoryService(CategoryAccess categoryAccess) {
        this.categoryAccess = categoryAccess;
    }

    // Retrieve all categories from storage
    public List<CategoryModel> getAllCategories() {
        return categoryAccess.findAll();
    }

    // Retrieve a single category by id
    public Optional<CategoryModel> getCategoryById(int id) {
        return categoryAccess.findById(id);
    }

    // Create a new category from the request payload
    public CategoryModel createCategory(CreateCategoryRequest request) {
        CategoryModel category = buildFromRequest(request);
        int generatedId = categoryAccess.insert(category);
        category.setCategoryId(generatedId);
        return category;
    }

    // Update an existing category using provided fields
    public Optional<CategoryModel> updateCategory(int id, UpdateCategoryRequest request) {
        return categoryAccess.findById(id).map(existing -> {
            mergeRequest(existing, request);
            categoryAccess.update(existing);
            return existing;
        });
    }

    // Remove a category by id
    public void deleteCategory(int id) {
        categoryAccess.deleteById(id);
    }

    // Build category model from create request
    private CategoryModel buildFromRequest(CreateCategoryRequest request) {
        CategoryModel category = new CategoryModel();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    // Apply non-null update fields to the category model
    private void mergeRequest(CategoryModel category, UpdateCategoryRequest request) {
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
    }
}
