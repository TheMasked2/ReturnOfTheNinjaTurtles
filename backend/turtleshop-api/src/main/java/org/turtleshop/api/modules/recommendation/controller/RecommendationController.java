package org.turtleshop.api.modules.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.recommendation.dto.RecommendedProduct;
import org.turtleshop.api.modules.recommendation.service.RecommendationService;

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
            @RequestParam(defaultValue = "4") int limit) {

        List<RecommendedProduct> recommendations = this.recommendationService.getHydratedRecommendations(customerId, limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/popular/month")
    public ResponseEntity<List<RecommendedProduct>> getPopularProductsThisMonth(
            @RequestParam(defaultValue = "4") int limit) {

        List<RecommendedProduct> recommendations = this.recommendationService.getTopProductsSoldThisMonth(limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/popular/season")
    public ResponseEntity<List<RecommendedProduct>> getPopularProductsThisSeason(
            @RequestParam(defaultValue = "4") int limit) {

        List<RecommendedProduct> recommendations = this.recommendationService.getTopProductsSoldThisSeason(limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/frequently-bought-together")
    public ResponseEntity<List<RecommendedProduct>> getFrequentlyBoughtTogether(
            @RequestParam int productId,
            @RequestParam(defaultValue = "4") int limit) {

        List<RecommendedProduct> recommendations = this.recommendationService.getFrequentlyBoughtTogether(productId, limit);
        return ResponseEntity.ok(recommendations);
    }
}