package org.turtleshop.api.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration-test base for tests that need a real Redis instance.
 *
 * Existing integration tests can continue extending IntegrationTestBase.
 * Only Redis-related tests need to extend this class.
 */
public abstract class RedisIntegrationTestBase extends IntegrationTestBase {

    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                    .withExposedPorts(REDIS_PORT)
                    .waitingFor(Wait.forListeningPort());

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
        registry.add("spring.data.redis.timeout", () -> "2s");
    }
}
