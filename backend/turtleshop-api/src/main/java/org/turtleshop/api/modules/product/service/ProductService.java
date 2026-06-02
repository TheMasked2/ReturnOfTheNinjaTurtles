package org.turtleshop.api.modules.product.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.UpdateProductRequest;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.repository.ProductAccess;
import org.turtleshop.api.modules.product.repository.ProductMongoAccess;

@Service
public class ProductService {
    private final ProductAccess productAccess;
    private final ProductMongoAccess productMongoAccess;

    public ProductService(ProductAccess productAccess, ProductMongoAccess productMongoAccess) {
        this.productAccess = productAccess;
        this.productMongoAccess = productMongoAccess;
    }

    public List<ProductModel> getAllProducts() {
        List<ProductModel> sqlProducts = productAccess.findAll();
        if (sqlProducts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> productIds = sqlProducts.stream()
                .map(ProductModel::getProductId)
                .collect(Collectors.toList());

        Map<Integer, ProductModel> mongoProducts = productMongoAccess.findAllByProductIds(productIds).stream()
                .collect(Collectors.toMap(ProductModel::getProductId, product -> product));

        return sqlProducts.stream()
                .map(base -> merge(base, mongoProducts.get(base.getProductId())))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "frequent_products", key = "#id", unless = "#result.isEmpty()")
    public Optional<ProductModel> getProductById(int id) {
        return productAccess.findById(id)
                .map(base -> merge(base, productMongoAccess.findByProductId(id).orElse(null)));
    }

    @CachePut(value = "frequent_products", key = "#result.productId")
    public ProductModel createProduct(CreateProductRequest request) {
        ProductModel product = buildFromRequest(request);
        int generatedId = productAccess.insert(product);
        product.setProductId(generatedId);
        productMongoAccess.save(product);
        return product;
    }

    @CacheEvict(value = "frequent_products", key = "#id")
    public Optional<ProductModel> updateProduct(int id, UpdateProductRequest request) {
        return productAccess.findById(id).map(base -> {
            base.setBasePrice(request.getPrice());
            productAccess.update(base);

            ProductModel mongoDocument = productMongoAccess.findByProductId(id)
                    .orElseGet(ProductModel::new);

            mongoDocument.setProductId(id);
            mergeBaseAndRequest(mongoDocument, request);

            productMongoAccess.save(mongoDocument);
            return merge(base, mongoDocument);
        });
    }

    @CacheEvict(value = "frequent_products", key = "#id")
    public void deleteProduct(int id) {
        productAccess.deleteById(id);
        productMongoAccess.deleteByProductId(id);
    }

    private ProductModel merge(ProductModel base, ProductModel mongo) {
        if (mongo == null) {
            return base;
        }

        base.setMongoProductId(mongo.getMongoProductId());
        base.setProductName(mongo.getProductName());
        base.setDescription(mongo.getDescription());
        base.setSpecs(mongo.getSpecs());
        base.setAvailableSince(mongo.getAvailableSince());
        base.setSuggestedProducts(mongo.getSuggestedProducts());
        return base;
    }

    private ProductModel buildFromRequest(CreateProductRequest request) {
        ProductModel product = new ProductModel();
        product.setBasePrice(request.getPrice());
        product.setProductName(request.getName());
        product.setDescription(request.getDescription());
        product.setSpecs(request.getSpecs());
        product.setAvailableSince(request.getAvailableSince());
        product.setSuggestedProducts(request.getSuggestedProducts());
        return product;
    }

    private void mergeBaseAndRequest(ProductModel mongoDocument, UpdateProductRequest request) {
        if (request.getName() != null) {
            mongoDocument.setProductName(request.getName());
        }
        if (request.getDescription() != null) {
            mongoDocument.setDescription(request.getDescription());
        }
        if (request.getSpecs() != null) {
            mongoDocument.setSpecs(request.getSpecs());
        }
        if (request.getAvailableSince() != null) {
            mongoDocument.setAvailableSince(request.getAvailableSince());
        }
        if (request.getSuggestedProducts() != null) {
            mongoDocument.setSuggestedProducts(request.getSuggestedProducts());
        }
    }
}