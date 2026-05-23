package org.turtleshop.api.modules.reviews.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.reviews.dto.ReviewRequest;
import org.turtleshop.api.modules.reviews.dto.ReviewResponse;
import org.turtleshop.api.modules.reviews.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ReviewService reviewService;

    @PostMapping 
    public ResponseEntity<ReviewResponse> createReview( 
        @PathVariable Integer productId, 
        @RequestBody ReviewRequest request 
    )
    { 
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reviewService.createReview(productId, request));
    }

    @GetMapping  // GET /api/products/1/reviews
    public ResponseEntity<List<ReviewResponse>> getProductReviews(
            @PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }
}