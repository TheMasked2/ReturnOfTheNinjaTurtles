package org.turtleshop.api.modules.category.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.category.dto.CategoryResponse;
import org.turtleshop.api.modules.category.dto.CreateCategoryRequest;
import org.turtleshop.api.modules.category.dto.UpdateCategoryRequest;
import org.turtleshop.api.modules.category.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
// Exposes category CRUD endpoints
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    // Return all category records
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    // Return a category by its identifier
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable int id) {
        return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(new CategoryResponse(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    // Create a new category record
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CreateCategoryRequest request) {
        var category = categoryService.createCategory(request);
        return ResponseEntity.created(URI.create("/api/categories/" + category.getCategoryId()))
                .body(new CategoryResponse(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    // Update category fields by id
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable int id, @RequestBody UpdateCategoryRequest request) {
        return categoryService.updateCategory(id, request)
                .map(category -> ResponseEntity.ok(new CategoryResponse(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    // Delete a category by id
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
