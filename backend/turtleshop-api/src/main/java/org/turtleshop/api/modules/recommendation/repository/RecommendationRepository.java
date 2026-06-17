package org.turtleshop.api.modules.recommendation.repository;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RecommendationRepository {

    private final Neo4jClient neo4jClient;

    public RecommendationRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public List<Integer> getSeasonalCollaborativeRecommendations(String customerId, String currentSeason, int limit) {
        String cypher =
            "MATCH (target:Customer {id: $customer_id}) " +
            "MATCH (target)-[:PLACED|WROTE]->(interaction)-[:CONTAINS|REVIEWS]->(p:Product) " +
            "WITH target, collect(distinct p) AS targetProducts " +
            "MATCH (s:Season {id: $currentSeason})<-[:PART_OF_SEASON]-(:Month)<-[:PLACED_IN|WRITTEN_IN]-(otherInteraction)-[:CONTAINS|REVIEWS]->(p:Product) " +
            "WHERE p IN targetProducts " +
            "MATCH (otherCustomer:Customer)-[:PLACED|WROTE]->(otherInteraction) " +
            "WHERE otherCustomer <> target " +
            "MATCH (otherCustomer)-[:PLACED|WROTE]->(recommendationInteraction)-[:CONTAINS|REVIEWS]->(recProduct:Product) " +
            "MATCH (recommendationInteraction)-[:PLACED_IN|WRITTEN_IN]->(:Month)-[:PART_OF_SEASON]->(s) " +
            "WHERE NOT recProduct IN targetProducts " +
            "RETURN recProduct.id AS product_id, count(distinct otherCustomer) AS score " +
            "ORDER BY score DESC " +
            "LIMIT $limit";

        return new ArrayList<>(this.neo4jClient.query(cypher)
                .bind(customerId).to("customer_id")
                .bind(currentSeason).to("currentSeason")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("product_id").asInt())
                .all());
    }

    public List<Integer> getTopProductsByCurrentMonth(String monthId, int limit) {
        String cypher =
            "MATCH (m:Month {id: $monthId}) " +
            "MATCH (m)<-[:PLACED_IN]-(o:Order)-[rel:CONTAINS]->(p:Product) " +
            "RETURN p.id AS product_id, sum(rel.quantity) AS sold " +
            "ORDER BY sold DESC " +
            "LIMIT $limit";

        return new ArrayList<>(this.neo4jClient.query(cypher)
                .bind(monthId).to("monthId")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("product_id").asInt())
                .all());
    }

    public List<Integer> getTopProductsByCurrentSeason(String seasonId, int limit) {
        String cypher =
            "MATCH (s:Season {id: $seasonId}) " +
            "MATCH (s)<-[:PART_OF_SEASON]-(m:Month)<-[:PLACED_IN]-(o:Order)-[rel:CONTAINS]->(p:Product) " +
            "RETURN p.id AS product_id, sum(rel.quantity) AS sold " +
            "ORDER BY sold DESC " +
            "LIMIT $limit";

        return new ArrayList<>(this.neo4jClient.query(cypher)
                .bind(seasonId).to("seasonId")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("product_id").asInt())
                .all());
    }

    public List<Integer> getFrequentlyBoughtTogether(int productId, int limit) {
        String cypher =
            "MATCH (p1:Product {id: $productId})<-[:CONTAINS]-(o:Order)-[:CONTAINS]->(p2:Product) " +
            "WHERE p1 <> p2 " +
            "RETURN p2.id AS product_id, count(DISTINCT o) AS frequency " +
            "ORDER BY frequency DESC " +
            "LIMIT $limit";

        return new ArrayList<>(this.neo4jClient.query(cypher)
                .bind(productId).to("productId")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("product_id").asInt())
                .all());
    }
}