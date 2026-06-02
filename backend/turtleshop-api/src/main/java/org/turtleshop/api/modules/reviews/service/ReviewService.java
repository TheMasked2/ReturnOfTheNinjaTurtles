package org.turtleshop.api.modules.reviews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.reviews.dto.ReviewRequest;
import org.turtleshop.api.modules.reviews.dto.ReviewResponse;
import org.turtleshop.api.modules.reviews.dto.ReviewStatsResponse;
import org.turtleshop.api.modules.reviews.model.ReviewModel;
import org.turtleshop.api.modules.reviews.repository.ReviewAccess;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.Neo4jClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewAccess repository;
    private final SequenceGeneratorService sequenceGenerator;
    private final MongoTemplate mongoTemplate;
    private final Neo4jClient neo4jClient;

    public ReviewResponse createReview(ReviewRequest request) {
        ReviewModel review = ReviewModel.builder()
                .reviewId(sequenceGenerator.generateSequence("review_sequence"))
                .productId(request.getProductId())
                .customerId(request.getCustomerId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(OffsetDateTime.now())
                .build();

        ReviewModel savedReview = repository.save(review);

        String currentMonthId = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        this.neo4jClient.query(
                "MATCH (m:Month {id: $monthId}) " +
                "MERGE (c:Customer {id: $customer_id}) " +
                "MERGE (p:Product {id: $product_id}) " +
                "MERGE (r:Review {id: $review_id}) " +
                "ON CREATE SET r.rating = $rating " +
                "MERGE (c)-[:WROTE]->(r) " +
                "MERGE (r)-[:REVIEWS]->(p) " +
                "MERGE (r)-[:WRITTEN_IN]->(m)"
        )
        .bind(currentMonthId).to("monthId")
        .bind(savedReview.getCustomerId().toString()).to("customer_id")
        .bind(savedReview.getProductId()).to("product_id")
        .bind(savedReview.getReviewId()).to("review_id")
        .bind(savedReview.getRating()).to("rating")
        .run();

        return mapToResponse(savedReview);
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

    public ReviewResponse updateReview(String id, ReviewRequest request) {
        ReviewModel review = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setProductId(request.getProductId());
        review.setCustomerId(request.getCustomerId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return mapToResponse(repository.save(review));
    }

    public void deleteReview(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        repository.deleteById(id);
    }

    public ReviewStatsResponse getProductReviewStats(Integer productId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("product_id").is(productId)),
                Aggregation.group("product_id")
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