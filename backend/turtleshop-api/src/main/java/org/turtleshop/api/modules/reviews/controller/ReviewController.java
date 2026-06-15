package org.turtleshop.api.modules.reviews.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.reviews.dto.ReviewRequest;
import org.turtleshop.api.modules.reviews.dto.ReviewResponse;
import org.turtleshop.api.modules.reviews.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")  // GET /api/reviews/abc123
    public ResponseEntity<ReviewResponse> getReview(@PathVariable String id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }
    
    @PutMapping("/{id}")  // PUT /api/reviews/abc123
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }
    
    @DeleteMapping("/{id}")  // DELETE /api/reviews/abc123
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}