package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.turtleshop.api.config.GraphIntegrationTestBase;
import org.turtleshop.api.modules.recommendation.repository.BackfillGraphRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GraphBackfillRepositoryIntegrationTest extends GraphIntegrationTestBase {

    @Autowired
    private BackfillGraphRepository backfillGraphRepository;

    @Test
    void batchInsertOrders_shouldCreateOrderGraph() {
        String customerId = UUID.randomUUID().toString();

        backfillGraphRepository.batchInsertOrders(List.of(
                Map.of(
                        "order_id", 1,
                        "customer_id", customerId,
                        "product_id", 10,
                        "product_name", "Pizza Box",
                        "quantity", 3,
                        "month_id", "2026-06"
                )
        ));

        Boolean exists = neo4jClient.query("""
                MATCH (c:Customer {id: $customerId})-[:PLACED]->(o:Order {id: 1})
                MATCH (o)-[r:CONTAINS]->(p:Product {id: 10, name: "Pizza Box"})
                MATCH (o)-[:PLACED_IN]->(:Month {id: "2026-06"})-[:PART_OF_SEASON]->(:Season {id: "Summer"})
                RETURN r.quantity = 3 AS ok
                """)
                .bind(customerId).to("customerId")
                .fetchAs(Boolean.class)
                .mappedBy((typeSystem, record) -> record.get("ok").asBoolean())
                .one()
                .orElse(false);

        assertThat(exists).isTrue();
    }

    @Test
    void batchInsertReviews_shouldCreateReviewGraph() {
        String customerId = UUID.randomUUID().toString();

        backfillGraphRepository.batchInsertReviews(List.of(
                Map.of(
                        "review_id", 500,
                        "customer_id", customerId,
                        "product_id", 10,
                        "rating", 5,
                        "month_id", "2026-06"
                )
        ));

        Boolean exists = neo4jClient.query("""
                MATCH (c:Customer {id: $customerId})-[:WROTE]->(r:Review {id: 500})
                MATCH (r)-[:REVIEWS]->(p:Product {id: 10})
                MATCH (r)-[:WRITTEN_IN]->(:Month {id: "2026-06"})-[:PART_OF_SEASON]->(:Season {id: "Summer"})
                RETURN r.rating = 5 AS ok
                """)
                .bind(customerId).to("customerId")
                .fetchAs(Boolean.class)
                .mappedBy((typeSystem, record) -> record.get("ok").asBoolean())
                .one()
                .orElse(false);

        assertThat(exists).isTrue();
    }
}