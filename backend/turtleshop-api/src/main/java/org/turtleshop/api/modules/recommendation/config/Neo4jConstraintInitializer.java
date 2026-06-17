package org.turtleshop.api.modules.recommendation.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class Neo4jConstraintInitializer {

    private final Driver driver;

    public Neo4jConstraintInitializer(Driver driver) {
        this.driver = driver;
    }

    @PostConstruct
    public void initializeConstraints() {
        try (Session session = this.driver.session()) {
            session.run("CREATE CONSTRAINT unique_customer_id IF NOT EXISTS FOR (c:Customer) REQUIRE c.id IS UNIQUE");
            session.run("CREATE CONSTRAINT unique_product_id IF NOT EXISTS FOR (p:Product) REQUIRE p.id IS UNIQUE");
            session.run("CREATE CONSTRAINT unique_month_id IF NOT EXISTS FOR (m:Month) REQUIRE m.id IS UNIQUE");
            session.run("CREATE CONSTRAINT unique_season_id IF NOT EXISTS FOR (s:Season) REQUIRE s.id IS UNIQUE");
            session.run("CREATE CONSTRAINT unique_order_id IF NOT EXISTS FOR (o:Order) REQUIRE o.id IS UNIQUE");
            session.run("CREATE CONSTRAINT unique_review_id IF NOT EXISTS FOR (r:Review) REQUIRE r.id IS UNIQUE");
        }
    }
}