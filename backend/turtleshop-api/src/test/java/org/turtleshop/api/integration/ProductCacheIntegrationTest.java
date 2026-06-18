package org.turtleshop.api.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.turtleshop.api.config.RedisIntegrationTestBase;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.UpdateProductRequest;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.repository.ProductAccess;
import org.turtleshop.api.modules.product.repository.ProductMongoAccess;
import org.turtleshop.api.modules.product.service.ProductService;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = "/db/testdata/product-cache-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class ProductCacheIntegrationTest extends RedisIntegrationTestBase {

    private static final int TEST_PRODUCT_ID = 1001;
    private static final String CACHE_NAME = "frequent_products";
    private static final String TEST_CACHE_KEY = CACHE_NAME + "::" + TEST_PRODUCT_ID;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAccess productAccess;

    @Autowired
    private ProductMongoAccess productMongoAccess;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final List<Integer> dynamicallyCreatedProductIds = new ArrayList<>();

    @BeforeEach
    void prepareMongoProductAndClearCache() {
        clearProductCache();
        productMongoAccess.deleteByProductId(TEST_PRODUCT_ID);
        productMongoAccess.save(buildMongoProduct(TEST_PRODUCT_ID));
    }

    @AfterEach
    void cleanUp() {
        clearProductCache();
        productMongoAccess.deleteByProductId(TEST_PRODUCT_ID);

        for (int productId : dynamicallyCreatedProductIds) {
            productMongoAccess.deleteByProductId(productId);
            productAccess.deleteById(productId);
        }
        dynamicallyCreatedProductIds.clear();
    }

    @Test
    void getProductById_whenCalledTwice_shouldReturnSecondResultFromRedis() {
        Optional<ProductModel> firstResult = productService.getProductById(TEST_PRODUCT_ID);

        assertThat(firstResult).isPresent();
        assertThat(firstResult.orElseThrow().getProductName())
                .isEqualTo("Redis Integration Product");
        assertThat(Boolean.TRUE.equals(redisTemplate.hasKey(TEST_CACHE_KEY))).isTrue();

        String cachedJson = redisTemplate.opsForValue().get(TEST_CACHE_KEY);
        assertThat(cachedJson)
                .contains("Redis Integration Product")
                .contains("2024-02-01");

        // Remove the source records without going through ProductService.
        // A second successful read therefore proves that Spring returned the cached value.
        productAccess.deleteById(TEST_PRODUCT_ID);
        productMongoAccess.deleteByProductId(TEST_PRODUCT_ID);

        Optional<ProductModel> secondResult = productService.getProductById(TEST_PRODUCT_ID);

        assertThat(secondResult).isPresent();
        assertThat(secondResult.orElseThrow().getProductId()).isEqualTo(TEST_PRODUCT_ID);
        assertThat(secondResult.orElseThrow().getProductName())
                .isEqualTo("Redis Integration Product");
        assertThat(secondResult.orElseThrow().getAvailableSince())
                .isEqualTo(LocalDate.of(2024, 2, 1));
    }

    @Test
    void getProductById_shouldStoreKeyWithConfiguredFifteenMinuteTtl() {
        productService.getProductById(TEST_PRODUCT_ID);

        Long ttlSeconds = redisTemplate.getExpire(TEST_CACHE_KEY, TimeUnit.SECONDS);

        assertThat(ttlSeconds).isNotNull();
        assertThat(ttlSeconds).isBetween(1L, 900L);
    }

    @Test
    void getProductById_whenProductDoesNotExist_shouldNotCacheEmptyOptional() {
        int missingProductId = 999_999;
        String missingKey = CACHE_NAME + "::" + missingProductId;

        Optional<ProductModel> result = productService.getProductById(missingProductId);

        assertThat(result).isEmpty();
        assertThat(Boolean.TRUE.equals(redisTemplate.hasKey(missingKey))).isFalse();
    }

    @Test
    void deleteProduct_shouldEvictCachedProduct() {
        productService.getProductById(TEST_PRODUCT_ID);
        assertThat(Boolean.TRUE.equals(redisTemplate.hasKey(TEST_CACHE_KEY))).isTrue();

        productService.deleteProduct(TEST_PRODUCT_ID);

        assertThat(Boolean.TRUE.equals(redisTemplate.hasKey(TEST_CACHE_KEY))).isFalse();
        assertThat(productService.getProductById(TEST_PRODUCT_ID)).isEmpty();
    }

    @Test
    void createProduct_shouldPutCreatedProductDirectlyIntoCache() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("CachePut Product");
        request.setDescription("Created by ProductCacheIntegrationTest");
        request.setPrice(new BigDecimal("42.50"));
        request.setSpecs("Redis test product");
        request.setAvailableSince(LocalDate.of(2026, 1, 10));
        request.setSuggestedProducts(List.of("TMNT-001", "TMNT-003"));

        ProductModel created = productService.createProduct(request);
        dynamicallyCreatedProductIds.add(created.getProductId());

        String createdKey = CACHE_NAME + "::" + created.getProductId();
        assertThat(Boolean.TRUE.equals(redisTemplate.hasKey(createdKey))).isTrue();

        // Remove backing records directly. The service should still return the @CachePut value.
        productAccess.deleteById(created.getProductId());
        productMongoAccess.deleteByProductId(created.getProductId());

        ProductModel cachedProduct = productService.getProductById(created.getProductId()).orElseThrow();
        assertThat(cachedProduct.getProductName()).isEqualTo("CachePut Product");
        assertThat(cachedProduct.getAvailableSince()).isEqualTo(LocalDate.of(2026, 1, 10));
    }

    private ProductModel buildMongoProduct(int productId) {
        ProductModel product = new ProductModel();
        product.setProductId(productId);
        product.setProductName("Redis Integration Product");
        product.setDescription("Product used to verify Redis caching");
        product.setSpecs("Integration-test specs");
        product.setAvailableSince(LocalDate.of(2024, 2, 1));
        product.setSuggestedProducts(List.of("TMNT-001", "TMNT-003"));
        return product;
    }

    private void clearProductCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        assertThat(cache).as("Redis cache '%s' should exist", CACHE_NAME).isNotNull();
        cache.clear();
    }
}
