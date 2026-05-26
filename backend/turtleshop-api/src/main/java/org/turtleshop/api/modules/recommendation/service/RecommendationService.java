package org.turtleshop.api.modules.recommendation.service;

import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.recommendation.model.ProductNode;
import org.turtleshop.api.modules.recommendation.repository.RecommendationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    public void processPurchaseSync(UUID customerId, Integer productId, String productName) {
        this.recommendationRepository.syncPurchase(customerId, productId, productName);
    }

    public List<ProductNode> getProductRecommendations(Integer productId, int limit) {
        return this.recommendationRepository.getCollaborativeRecommendations(productId, limit);
    }
}