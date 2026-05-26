package org.turtleshop.api.modules.recommendation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.recommendation.model.ProductNode;

@Repository
public interface RecommendationRepository extends Neo4jRepository<ProductNode, Integer> {

    @Query("MERGE (c:Customer {id: $customerId}) " +
           "MERGE (p:Product {id: $productId, name: $productName}) " +
           "MERGE (c)-[:BOUGHT]->(p)")
    void syncPurchase(@Param("customerId") UUID customerId, 
                      @Param("productId") Integer productId, 
                      @Param("productName") String productName);

    @Query("MATCH (target:Product {id: $productId})<-[:BOUGHT]-(c:Customer)-[:BOUGHT]->(reco:Product) " +
           "WHERE reco.id <> $productId " +
           "RETURN reco " +
           "ORDER BY COUNT(c) DESC " +
           "LIMIT $limit")
    List<ProductNode> getCollaborativeRecommendations(@Param("productId") Integer productId, 
                                                      @Param("limit") int limit);
}