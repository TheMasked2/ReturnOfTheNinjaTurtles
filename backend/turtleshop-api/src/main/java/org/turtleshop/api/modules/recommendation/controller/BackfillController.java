package org.turtleshop.api.modules.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.recommendation.service.BackfillService;

@RestController
@RequestMapping("/api/recommendations/backfill")
public class BackfillController {

    private final BackfillService backfillService;

    public BackfillController(BackfillService backfillService) {
        this.backfillService = backfillService;
    }

    @PostMapping("/orders")
    public ResponseEntity<String> backfillOrders() {
        this.backfillService.executeOrderDataBackfill();
        return ResponseEntity.ok("Historical relational order data backfill initiated successfully.");
    }

    // Resolving the caveat: Exposing the NoSQL streaming extraction pathway
    @PostMapping("/reviews")
    public ResponseEntity<String> backfillReviews() {
        this.backfillService.executeReviewDataBackfill();
        return ResponseEntity.ok("Historical NoSQL review data backfill initiated successfully.");
    }
}