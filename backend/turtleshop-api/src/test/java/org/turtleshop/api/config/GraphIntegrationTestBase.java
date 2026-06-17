package org.turtleshop.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.turtleshop.api.modules.recommendation.config.TimeTreeInitializer;

public abstract class GraphIntegrationTestBase extends IntegrationTestBase {

    private static final Neo4jContainer<?> NEO4J =
            new Neo4jContainer<>("neo4j:5.12.0-community")
                    .withAdminPassword("testpassword")
                    .withEnv("NEO4J_PLUGINS", "[\"apoc\"]")
                    .withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.*");

    static {
        NEO4J.start();
    }

    @DynamicPropertySource
    static void configureNeo4j(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", NEO4J::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "testpassword");
    }

    @Autowired
    protected Neo4jClient neo4jClient;

    @Autowired
    private TimeTreeInitializer timeTreeInitializer;

    @BeforeEach
    void cleanGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        timeTreeInitializer.buildTemporalBackbone();
    }
}