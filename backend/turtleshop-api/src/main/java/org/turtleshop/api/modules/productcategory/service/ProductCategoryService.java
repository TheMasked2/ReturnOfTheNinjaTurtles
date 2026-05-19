package org.turtleshop.api.modules.productcategory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.productcategory.dto.CreateProductCategoryRequest;
import org.turtleshop.api.modules.productcategory.dto.UpdateProductCategoryRequest;
import org.turtleshop.api.modules.productcategory.model.ProductCategoryModel;
import org.turtleshop.api.modules.productcategory.repository.ProductCategoryAccess;

@Service
// Business logic for product-category mappings
public class ProductCategoryService {
    private final ProductCategoryAccess productCategoryAccess;

    public ProductCategoryService(ProductCategoryAccess productCategoryAccess) {
        this.productCategoryAccess = productCategoryAccess;
    }

    // Retrieve all product-category mappings
    public List<ProductCategoryModel> getAllProductCategories() {
        return productCategoryAccess.findAll();
    }

    // Retrieve a mapping by ids
    public Optional<ProductCategoryModel> getProductCategory(int productId, int categoryId) {
        return productCategoryAccess.findById(productId, categoryId);
    }

    // Create a new product-category mapping
    public ProductCategoryModel createProductCategory(CreateProductCategoryRequest request) {
        ProductCategoryModel mapping = new ProductCategoryModel();
        mapping.setProductId(request.getProductId());
        mapping.setCategoryId(request.getCategoryId());
        productCategoryAccess.insert(mapping);
        return mapping;
    }

    // Update an existing mapping
    public Optional<ProductCategoryModel> updateProductCategory(int productId, int categoryId, UpdateProductCategoryRequest request) {
        return productCategoryAccess.findById(productId, categoryId)
                .map(existing -> {
                    int newProductId = request.getNewProductId() != null ? request.getNewProductId() : existing.getProductId();
                    int newCategoryId = request.getNewCategoryId() != null ? request.getNewCategoryId() : existing.getCategoryId();
                    ProductCategoryModel updated = new ProductCategoryModel();
                    updated.setProductId(newProductId);
                    updated.setCategoryId(newCategoryId);
                    productCategoryAccess.update(productId, categoryId, updated);
                    return updated;
                });
    }

    // Delete a mapping by ids
    public void deleteProductCategory(int productId, int categoryId) {
        productCategoryAccess.deleteById(productId, categoryId);
    }
}
