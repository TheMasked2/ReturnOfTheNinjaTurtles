package org.turtleshop.api.modules.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.turtleshop.api.modules.product.model.ProductModel;

public interface ProductRepository extends MongoRepository<ProductModel, String> {
}