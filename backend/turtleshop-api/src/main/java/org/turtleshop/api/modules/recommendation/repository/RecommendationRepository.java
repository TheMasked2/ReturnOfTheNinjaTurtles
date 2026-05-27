package org.turtleshop.api.modules.recommendation.repository;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class RecommendationRepository {

    private final Neo4jClient neo4jClient;

    public RecommendationRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /**
     * Algoritme: Vind producten die populair zijn in het huidige seizoen 
     * onder klanten met vergelijkbare aankoop- en reviewpatronen.
     */
    public List<Integer> getSeasonalCollaborativeRecommendations(String customerId, String currentSeason, int limit) {
        String cypher = 
            "MATCH (target:Customer {id: $customer_id}) " +
            "MATCH (target)-[:PLACED|WROTE]->(interaction)-[:CONTAINS|REVIEWS]->(p:Product) " +
            "WITH target, collect(distinct p) AS targetProducts " +
            
            // Vind andere klanten die dezelfde producten hebben gekocht/beoordeeld in dit specifieke seizoen
            "MATCH (s:Season {id: $currentSeason})<-[:PART_OF_SEASON]-(:Month)<-[:PLACED_IN|WRITTEN_IN]-(otherInteraction)-[:CONTAINS|REVIEWS]->(p:Product) " +
            "WHERE p IN targetProducts " +
            "MATCH (otherCustomer:Customer)-[:PLACED|WROTE]->(otherInteraction) " +
            "WHERE otherCustomer <> target " +
            
            // Vind alternatieve producten die zij in DIT seizoen hebben gekocht of hoog hebben beoordeeld
            "MATCH (otherCustomer)-[:PLACED|WROTE]->(recommendationInteraction)-[:CONTAINS|REVIEWS]->(recProduct:Product) " +
            "MATCH (recommendationInteraction)-[:PLACED_IN|WRITTEN_IN]->(:Month)-[:PART_OF_SEASON]->(s) " +
            "WHERE NOT recProduct IN targetProducts " +
            
            // Bereken aanbevelingsscore op basis van interactiefrequentie
            "RETURN recProduct.id AS product_id, count(distinct otherCustomer) AS score " +
            "ORDER BY score DESC " +
            "LIMIT $limit";

        return (List<Integer>) this.neo4jClient.query(cypher)
                .bind(customerId).to("customer_id")
                .bind(currentSeason).to("currentSeason")
                .bind(limit).to("limit")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("product_id").asInt())
                .all();
    }
}