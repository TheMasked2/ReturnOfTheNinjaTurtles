package org.turtleshop.api.modules.product.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.ProductPageResponse;
import org.turtleshop.api.modules.product.dto.ProductResponse;
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

    public ProductPageResponse getAllProducts(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int offset = safePage * safeSize;

        int totalElements = productAccess.countAll();
        List<ProductModel> sqlProducts = productAccess.findPage(safeSize, offset);

        List<Integer> productIds = sqlProducts.stream()
                .map(ProductModel::getProductId)
                .collect(Collectors.toList());

        Map<Integer, ProductModel> mongoProducts = productMongoAccess.findAllByProductIds(productIds).stream()
                .collect(Collectors.toMap(ProductModel::getProductId, product -> product));

        List<ProductResponse> content = sqlProducts.stream()
                .map(base -> new ProductResponse(merge(base, mongoProducts.get(base.getProductId()))))
                .collect(Collectors.toList());

        return new ProductPageResponse(content, safePage, safeSize, totalElements);
    }

    public List<ProductModel> getProductsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductModel> sqlProducts = productAccess.findAllByIds(ids);
        Map<Integer, ProductModel> mongoProducts = productMongoAccess.findAllByProductIds(ids).stream()
                .collect(Collectors.toMap(ProductModel::getProductId, product -> product));

        Map<Integer, ProductModel> sqlProductMap = sqlProducts.stream()
                .collect(Collectors.toMap(ProductModel::getProductId, product -> product));

        return ids.stream()
                .map(sqlProductMap::get)
                .filter(Objects::nonNull)
                .map(base -> merge(base, mongoProducts.get(base.getProductId())))
                .collect(Collectors.toList());
    }

    public Optional<ProductModel> getProductById(int id) {
        return productAccess.findAllByIds(Collections.singletonList(id)).stream()
                .findFirst()
                .map(base -> merge(base, productMongoAccess.findByProductId(id).orElse(null)));
    }

    public ProductModel createProduct(CreateProductRequest request) {
        ProductModel product = buildFromRequest(request);
        int generatedId = productAccess.insert(product);
        product.setProductId(generatedId);
        productMongoAccess.save(product);
        return product;
    }

    public Optional<ProductModel> updateProduct(int id, UpdateProductRequest request) {
        Optional<ProductModel> existingOpt = productAccess.findAllByIds(List.of(id)).stream().findFirst();
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductModel existing = existingOpt.get();
        ProductModel mongoDocument = productMongoAccess.findByProductId(id).orElse(new ProductModel());
        mongoDocument.setProductId(id);
        mergeBaseAndRequest(mongoDocument, request);

        productMongoAccess.save(mongoDocument);
        return Optional.of(merge(existing, mongoDocument));
    }

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