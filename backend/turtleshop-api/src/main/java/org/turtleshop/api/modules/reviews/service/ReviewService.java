package org.turtleshop.api.modules.reviews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.reviews.dto.ReviewRequest;
import org.turtleshop.api.modules.reviews.dto.ReviewResponse;
import org.turtleshop.api.modules.reviews.dto.ReviewStatsResponse;
import org.turtleshop.api.modules.reviews.model.ReviewModel;
import org.turtleshop.api.modules.reviews.repository.ReviewAccess;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewAccess repository;
    private final SequenceGeneratorService sequenceGenerator;
    private final MongoTemplate mongoTemplate;

    public ReviewResponse createReview(Integer productId, ReviewRequest request) {
        validateProductExists(productId);

        ReviewModel review = ReviewModel.builder()
                .reviewId(sequenceGenerator.generateSequence("review_sequence"))
                .productId(productId)
                .customerId(request.getCustomerId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now().toString())
                .build();

        return mapToResponse(repository.save(review));
    }

    public ReviewResponse getReviewById(String id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    public List<ReviewResponse> getReviewsByProductId(Integer productId) {
        return repository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> searchReviews(String query) {
        return repository.findByCommentContainingIgnoreCase(query)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getAllReviews() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse updateReview(String reviewId, ReviewRequest request) {
        ReviewModel existingReview = repository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        validateProductExists(existingReview.getProductId());

        existingReview.setCustomerId(request.getCustomerId());
        existingReview.setRating(request.getRating());
        existingReview.setComment(request.getComment());

        return mapToResponse(repository.save(existingReview));
    }


    private void validateProductExists(Integer productId) {
        Query query = Query.query(
                Criteria.where("productId").is(productId)
        );

        boolean exists = mongoTemplate.exists(query, "products");

        if (!exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product does not exist"
            );
        }
    }

    public void deleteReview(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        repository.deleteById(id);
    }

    public ReviewStatsResponse getProductReviewStats(Integer productId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("productId").is(productId)),
                Aggregation.group("productId")
                        .count().as("reviewCount")
                        .avg("rating").as("averageRating")
        );

        AggregationResults<ReviewStatsResponse> results = mongoTemplate.aggregate(
                aggregation, "reviews", ReviewStatsResponse.class);

        return results.getUniqueMappedResult() != null
                ? results.getUniqueMappedResult()
                : ReviewStatsResponse.builder()
                    .productId(productId)
                    .reviewCount(0L)
                    .averageRating(0.0)
                    .build();
    }

    private ReviewResponse mapToResponse(ReviewModel review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewId(review.getReviewId())
                .productId(review.getProductId())
                .customerId(review.getCustomerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}