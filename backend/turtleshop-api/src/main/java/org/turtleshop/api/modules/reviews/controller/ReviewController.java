package org.turtleshop.api.modules.reviews.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.reviews.dto.ReviewRequest;
import org.turtleshop.api.modules.reviews.dto.ReviewResponse;
import org.turtleshop.api.modules.reviews.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable String id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REVIEW_CREATE_OWN') and @authorizationService.isReviewRequestOwner(#request, authentication)")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REVIEW_UPDATE_OWN') and @authorizationService.isReviewOwner(#id, authentication)")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REVIEW_DELETE_ALL') or " +
            "(hasAuthority('REVIEW_DELETE_OWN') and @authorizationService.isReviewOwner(#id, authentication))")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}