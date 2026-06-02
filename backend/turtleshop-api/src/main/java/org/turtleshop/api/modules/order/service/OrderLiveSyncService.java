package org.turtleshop.api.modules.order.service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OrderLiveSyncService {

    private final Neo4jClient neo4jClient;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public OrderLiveSyncService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public void syncOrderToGraph(int orderId, String customerId, int productId, String productName, int quantity) {
        String currentMonthId = LocalDate.now().format(MONTH_FORMATTER);

        String cypher = "MATCH (m:Month {id: $monthId}) " +
                        "MERGE (c:Customer {id: $customerId}) " +
                        "MERGE (p:Product {id: $productId, name: $productName}) " +
                        "MERGE (o:Order {id: $orderId}) " +
                        "MERGE (c)-[:PLACED]->(o) " +
                        "MERGE (o)-[:PLACED_IN]->(m) " +
                        "MERGE (o)-[:CONTAINS {quantity: $quantity}]->(p)";

        this.neo4jClient.query(cypher)
                .bind(currentMonthId).to("monthId")
                .bind(customerId).to("customerId")
                .bind(orderId).to("orderId")
                .bind(productId).to("productId")
                .bind(productName).to("productName")
                .bind(quantity).to("quantity")
                .run();
    }
}