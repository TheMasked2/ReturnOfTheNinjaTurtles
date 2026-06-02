package org.turtleshop.api.modules.productcategory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "categories", key = "'all_mappings'")
    public List<ProductCategoryModel> getAllProductCategories() {
        return productCategoryAccess.findAll();
    }

    @Cacheable(value = "categories", key = "#productId + ':' + #categoryId")
    public Optional<ProductCategoryModel> getProductCategory(int productId, int categoryId) {
        return productCategoryAccess.findById(productId, categoryId);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public ProductCategoryModel createProductCategory(CreateProductCategoryRequest request) {
        ProductCategoryModel mapping = new ProductCategoryModel();
        mapping.setProductId(request.getProductId());
        mapping.setCategoryId(request.getCategoryId());
        productCategoryAccess.insert(mapping);
        return mapping;
    }

    @CacheEvict(value = "categories", allEntries = true)
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

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteProductCategory(int productId, int categoryId) {
        productCategoryAccess.deleteById(productId, categoryId);
    }
}