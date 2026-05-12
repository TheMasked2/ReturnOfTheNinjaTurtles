package org.turtleshop.api.modules.product.repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.product.model.ProductModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductMongoAccess {
    private final MongoTemplate mongoTemplate;

    public Optional<ProductModel> findByProductId(int productId) {
        Query query = Query.query(Criteria.where("product_id").is(productId));
        return Optional.ofNullable(mongoTemplate.findOne(query, ProductModel.class));
    }

    public List<ProductModel> findAllByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        Query query = Query.query(Criteria.where("product_id").in(productIds));
        return mongoTemplate.find(query, ProductModel.class);
    }

    public ProductModel save(ProductModel product) {
        return mongoTemplate.save(product);
    }

    public void deleteByProductId(int productId) {
        Query query = Query.query(Criteria.where("product_id").is(productId));
        mongoTemplate.remove(query, ProductModel.class);
    }
}