package org.turtleshop.api.modules.productcategory.controller;

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
import org.turtleshop.api.modules.productcategory.dto.CreateProductCategoryRequest;
import org.turtleshop.api.modules.productcategory.dto.ProductCategoryResponse;
import org.turtleshop.api.modules.productcategory.dto.UpdateProductCategoryRequest;
import org.turtleshop.api.modules.productcategory.service.ProductCategoryService;

@RestController
@RequestMapping("/api/product-categories")
// Exposes CRUD endpoints for product-category mappings
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    // Return all product-category mappings
    public List<ProductCategoryResponse> getAllProductCategories() {
        return productCategoryService.getAllProductCategories().stream()
                .map(ProductCategoryResponse::new)
                .toList();
    }

    @GetMapping("/{productId}/{categoryId}")
    @PreAuthorize("permitAll()")
    // Return a mapping by product and category ids
    public ResponseEntity<ProductCategoryResponse> getProductCategory(
            @PathVariable int productId,
            @PathVariable int categoryId) {
        return productCategoryService.getProductCategory(productId, categoryId)
                .map(mapping -> ResponseEntity.ok(new ProductCategoryResponse(mapping)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE_ALL') and hasAuthority('CATEGORY_UPDATE_ALL')")
    // Create a new product-category mapping
    public ResponseEntity<ProductCategoryResponse> createProductCategory(
            @RequestBody CreateProductCategoryRequest request) {
        var mapping = productCategoryService.createProductCategory(request);
        return ResponseEntity.created(URI.create("/api/product-categories/" + mapping.getProductId() + "/" + mapping.getCategoryId()))
                .body(new ProductCategoryResponse(mapping));
    }

    @PutMapping("/{productId}/{categoryId}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE_ALL') and hasAuthority('CATEGORY_UPDATE_ALL')")
    // Update an existing product-category mapping
    public ResponseEntity<ProductCategoryResponse> updateProductCategory(
            @PathVariable int productId,
            @PathVariable int categoryId,
            @RequestBody UpdateProductCategoryRequest request) {
        return productCategoryService.updateProductCategory(productId, categoryId, request)
                .map(mapping -> ResponseEntity.ok(new ProductCategoryResponse(mapping)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{productId}/{categoryId}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE_ALL') and hasAuthority('CATEGORY_UPDATE_ALL')")
    // Delete a mapping by product and category ids
    public ResponseEntity<Void> deleteProductCategory(
            @PathVariable int productId,
            @PathVariable int categoryId) {
        productCategoryService.deleteProductCategory(productId, categoryId);
        return ResponseEntity.noContent().build();
    }
}