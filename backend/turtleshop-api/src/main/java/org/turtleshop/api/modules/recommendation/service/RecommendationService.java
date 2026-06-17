package org.turtleshop.api.modules.recommendation.service;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.recommendation.dto.RecommendedProduct;
import org.turtleshop.api.modules.recommendation.repository.RecommendationRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RecommendationService(RecommendationRepository recommendationRepository, 
                                 NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.recommendationRepository = recommendationRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<RecommendedProduct> getHydratedRecommendations(String customerId, int limit) {
        String currentSeason = determineCurrentSeason();
        List<Integer> productIds = recommendationRepository.getSeasonalCollaborativeRecommendations(customerId, currentSeason, limit);

        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Hydrate the relational assets from PostgreSQL using the graph database keys
        String sql = """
        SELECT product_id, product_name, base_price AS price, NULL AS image_url
        FROM PRODUCT
        WHERE product_id IN (:productIds)
        """;

        List<RecommendedProduct> hydratedProducts = namedParameterJdbcTemplate.query(
            sql, 
            Map.of("productIds", productIds),
            (rs, rowNum) -> new RecommendedProduct(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getBigDecimal("price"),
                rs.getString("image_url")
            )
        );

        // Re-sort the database result to preserve the recommendation affinity ranking calculated by Neo4j
        return productIds.stream()
            .flatMap(id -> hydratedProducts.stream().filter(p -> p.productId() == id))
            .toList();
    }

    private String determineCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        return switch (month) {
            case 12, 1, 2 -> "Winter";
            case 3, 4, 5 -> "Spring";
            case 6, 7, 8 -> "Summer";
            default -> "Autumn";
        };
    }
}