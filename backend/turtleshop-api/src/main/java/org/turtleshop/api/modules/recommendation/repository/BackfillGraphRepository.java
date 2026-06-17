package org.turtleshop.api.modules.recommendation.repository;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

@Repository
public class BackfillGraphRepository {

    private final Neo4jClient neo4jClient;

    public BackfillGraphRepository(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public void batchInsertOrders(Collection<Map<String, Object>> batch) {
        String cypher = "UNWIND $batch AS row " +
                "MATCH (m:Month {id: row.month_id}) " +
                "MERGE (c:Customer {id: row.customer_id}) " +
                "MERGE (p:Product {id: row.product_id}) " +
                "ON CREATE SET p.name = row.product_name " +
                "ON MATCH SET p.name = coalesce(p.name, row.product_name) " +
                "MERGE (o:Order {id: row.order_id}) " +
                "MERGE (c)-[:PLACED]->(o) " +
                "MERGE (o)-[:PLACED_IN]->(m) " +
                "MERGE (o)-[r:CONTAINS]->(p) " +
                "SET r.quantity = row.quantity";

        this.neo4jClient.query(cypher)
                .bind(batch).to("batch")
                .run();
    }

    public void batchInsertReviews(Collection<Map<String, Object>> batch) {
        String cypher = "UNWIND $batch AS row " +
                "MATCH (m:Month {id: row.month_id}) " +
                "MERGE (c:Customer {id: row.customer_id}) " +
                "MERGE (p:Product {id: row.product_id}) " +
                "MERGE (r:Review {id: row.review_id}) " +
                "ON CREATE SET r.rating = row.rating, r.comment = row.comment " +
                "MERGE (c)-[:WROTE]->(r) " +
                "MERGE (r)-[:REVIEWS]->(p) " +
                "MERGE (r)-[:WRITTEN_IN]->(m)";

        this.neo4jClient.query(cypher)
                .bind(batch).to("batch")
                .run();
    }
}