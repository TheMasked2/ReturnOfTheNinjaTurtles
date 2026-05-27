package org.turtleshop.api.modules.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.recommendation.service.RecommendationService;
import org.turtleshop.api.modules.recommendation.dto.RecommendedProduct;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/seasonal")
    public ResponseEntity<List<RecommendedProduct>> getSeasonalRecommendations(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<RecommendedProduct> recommendations = this.recommendationService.getHydratedRecommendations(customerId, limit);
        return ResponseEntity.ok(recommendations);
    }
}