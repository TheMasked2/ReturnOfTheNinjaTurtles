package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.turtleshop.api.config.GraphIntegrationTestBase;
import org.turtleshop.api.modules.recommendation.repository.RecommendationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationRepositoryIntegrationTest extends GraphIntegrationTestBase {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Test
    void getSeasonalCollaborativeRecommendations_shouldRecommendProductsFromSimilarCustomers() {
        String targetCustomer = UUID.randomUUID().toString();
        String similarCustomer = UUID.randomUUID().toString();

        String season = determineCurrentSeason();
        String monthId = LocalDate.now().toString().substring(0, 7);

        neo4jClient.query("""
                MERGE (s:Season {id: $season})
                MERGE (m:Month {id: $monthId})
                MERGE (m)-[:PART_OF_SEASON]->(s)

                MERGE (target:Customer {id: $targetCustomer})
                MERGE (similar:Customer {id: $similarCustomer})

                MERGE (sharedProduct:Product {id: 1, name: "Shared Product"})
                MERGE (recommendedProduct:Product {id: 2, name: "Recommended Product"})

                MERGE (targetOrder:Order {id: 100})
                MERGE (target)-[:PLACED]->(targetOrder)
                MERGE (targetOrder)-[:PLACED_IN]->(m)
                MERGE (targetOrder)-[:CONTAINS]->(sharedProduct)

                MERGE (similarOrder:Order {id: 200})
                MERGE (similar)-[:PLACED]->(similarOrder)
                MERGE (similarOrder)-[:PLACED_IN]->(m)
                MERGE (similarOrder)-[:CONTAINS]->(sharedProduct)
                MERGE (similarOrder)-[:CONTAINS]->(recommendedProduct)
                """)
                .bind(season).to("season")
                .bind(monthId).to("monthId")
                .bind(targetCustomer).to("targetCustomer")
                .bind(similarCustomer).to("similarCustomer")
                .run();

        List<Integer> recommendations =
                recommendationRepository.getSeasonalCollaborativeRecommendations(
                        targetCustomer,
                        season,
                        5
                );

        assertThat(recommendations).containsExactly(2);
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