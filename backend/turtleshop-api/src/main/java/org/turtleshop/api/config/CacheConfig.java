package org.turtleshop.api.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Uniform JSON serialization for visibility via Redis CLI
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()
            ));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(baseConfig.entryTtl(Duration.ofMinutes(10))) // Default fallback TTL
            .withInitialCacheConfigurations(Map.of(
                // Products: Volatile pool. Relies on Redis LFU to keep only popular items.
                "frequent_products", baseConfig.entryTtl(Duration.ofMinutes(15)),
                
                // Categories: Highly static, loaded constantly by navigation menus.
                "categories", baseConfig.entryTtl(Duration.ofHours(6))
            ))
            .build();
    }
}