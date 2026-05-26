package org.turtleshop.api.modules.recommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.recommendation.model.ProductNode;
import org.turtleshop.api.modules.recommendation.service.RecommendationService;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductNode>> getRecommendations(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductNode> recommendations = this.recommendationService.getProductRecommendations(productId, limit);
        return ResponseEntity.ok(recommendations);
    }
}