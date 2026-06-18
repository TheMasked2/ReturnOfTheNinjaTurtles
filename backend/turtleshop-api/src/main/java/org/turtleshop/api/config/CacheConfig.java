package org.turtleshop.api.config;

import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer();

        serializer.configure(objectMapper -> {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.registerModule(new Jdk8Module());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        });

        RedisCacheConfiguration defaultConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(serializer)
                        )
                        .entryTtl(Duration.ofMinutes(10));

        RedisCacheConfiguration productsConfiguration =
                defaultConfiguration.entryTtl(Duration.ofMinutes(15));

        RedisCacheConfiguration categoriesConfiguration =
                defaultConfiguration.entryTtl(Duration.ofHours(6));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(
                        Map.of(
                                "frequent_products",
                                productsConfiguration,
                                "categories",
                                categoriesConfiguration
                        )
                )
                .build();
    }
}