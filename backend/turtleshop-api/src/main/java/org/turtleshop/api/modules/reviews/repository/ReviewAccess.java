package org.turtleshop.api.modules.reviews.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.turtleshop.api.modules.reviews.model.ReviewModel;

import java.util.List;

public interface ReviewAccess extends MongoRepository<ReviewModel, String> {
    List<ReviewModel> findByProductId(Integer productId);
    List<ReviewModel> findByCustomerId(String customerId);
    List<ReviewModel> findByCommentContainingIgnoreCase(String comment);
}