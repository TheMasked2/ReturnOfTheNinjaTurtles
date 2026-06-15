package org.turtleshop.api.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class IntegrationTestBase {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("turtleshop_test")
                    .withUsername("test")
                    .withPassword("test");

    private static final MongoDBContainer MONGO =
            new MongoDBContainer("mongo:7.0");

    private static final Neo4jContainer<?> NEO4J =
            new Neo4jContainer<>("neo4j:5.12.0-community")
                    .withAdminPassword("testpassword")
                    .withEnv("NEO4J_PLUGINS", "[\"apoc\"]")
                    .withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.*")
                    .withEnv("NEO4J_dbms_security_procedures_allowlist", "apoc.*");

    static {
        POSTGRES.start();
        MONGO.start();
        NEO4J.start();
    }

    @DynamicPropertySource
    static void configureTestContainers(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);

        registry.add("spring.neo4j.uri", NEO4J::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "testpassword");
    }
}