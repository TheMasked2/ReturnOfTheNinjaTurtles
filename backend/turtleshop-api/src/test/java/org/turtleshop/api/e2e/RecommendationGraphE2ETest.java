package org.turtleshop.api.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.GraphIntegrationTestBase;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RecommendationGraphE2ETest extends GraphIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Neo4jClient neo4jClient;

    @Test
    void seasonalRecommendationsEndpoint_shouldReturnRecommendedProductFromGraphDb() throws Exception {
        String targetCustomerId = UUID.randomUUID().toString();
        String similarCustomerId = UUID.randomUUID().toString();

        String currentSeason = determineCurrentSeason();
        String currentMonthId = LocalDate.now().toString().substring(0, 7);

        /*
         * Test graph setup:
         *
         * Target customer bought Product 1.
         * Similar customer bought Product 1 and Product 2.
         *
         * Because both customers interacted with Product 1,
         * Product 2 should be recommended to the target customer.
         */
        neo4jClient.query("""
                MERGE (s:Season {id: $currentSeason})
                MERGE (m:Month {id: $currentMonthId})
                MERGE (m)-[:PART_OF_SEASON]->(s)

                MERGE (target:Customer {id: $targetCustomerId})
                MERGE (similar:Customer {id: $similarCustomerId})

                MERGE (sharedProduct:Product {id: 1, name: "Retro Vinyl Record"})
                MERGE (recommendedProduct:Product {id: 2, name: "Studio Headphones"})

                MERGE (targetOrder:Order {id: 10001})
                MERGE (target)-[:PLACED]->(targetOrder)
                MERGE (targetOrder)-[:PLACED_IN]->(m)
                MERGE (targetOrder)-[:CONTAINS {quantity: 1}]->(sharedProduct)

                MERGE (similarOrder:Order {id: 10002})
                MERGE (similar)-[:PLACED]->(similarOrder)
                MERGE (similarOrder)-[:PLACED_IN]->(m)
                MERGE (similarOrder)-[:CONTAINS {quantity: 1}]->(sharedProduct)
                MERGE (similarOrder)-[:CONTAINS {quantity: 1}]->(recommendedProduct)
                """)
                .bind(currentSeason).to("currentSeason")
                .bind(currentMonthId).to("currentMonthId")
                .bind(targetCustomerId).to("targetCustomerId")
                .bind(similarCustomerId).to("similarCustomerId")
                .run();

        mockMvc.perform(get("/api/recommendations/seasonal")
                        .param("customerId", targetCustomerId)
                        .param("limit", "5")
                        .with(user("graph-test-user@example.com")
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].productName").value("Studio Headphones"));
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