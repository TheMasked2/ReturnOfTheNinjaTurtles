package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.turtleshop.api.config.GraphIntegrationTestBase;
import org.turtleshop.api.modules.order.service.OrderLiveSyncService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLiveSyncGraphIntegrationTest extends GraphIntegrationTestBase {

    @Autowired
    private OrderLiveSyncService orderLiveSyncService;

    @Test
    void syncOrderToGraph_shouldCreateCustomerOrderProductAndRelationships() {
        int orderId = 100;
        String customerId = UUID.randomUUID().toString();
        int productId = 7;
        String productName = "Ninja Katana";
        int quantity = 2;

        orderLiveSyncService.syncOrderToGraph(
                orderId,
                customerId,
                productId,
                productName,
                quantity
        );

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Boolean exists = neo4jClient.query("""
                MATCH (c:Customer {id: $customerId})-[:PLACED]->(o:Order {id: $orderId})
                MATCH (o)-[r:CONTAINS]->(p:Product {id: $productId})
                MATCH (o)-[:PLACED_IN]->(m:Month {id: $monthId})
                RETURN p.name = $productName AND r.quantity = $quantity AS ok
                """)
                .bind(customerId).to("customerId")
                .bind(orderId).to("orderId")
                .bind(productId).to("productId")
                .bind(productName).to("productName")
                .bind(quantity).to("quantity")
                .bind(currentMonth).to("monthId")
                .fetchAs(Boolean.class)
                .mappedBy((typeSystem, record) -> record.get("ok").asBoolean())
                .one()
                .orElse(false);

        assertThat(exists).isTrue();
    }
}