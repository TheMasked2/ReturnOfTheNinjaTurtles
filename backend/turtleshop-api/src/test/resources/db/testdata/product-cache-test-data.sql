-- product-cache-test-data.sql
-- Product used only by ProductCacheIntegrationTest.
-- It is intentionally outside the normal seed range of products 1-10.

INSERT INTO PRODUCT (
    product_id,
    product_name,
    base_price
)
VALUES (
           1001,
           'Redis Integration Product',
           19.99
)
ON CONFLICT (product_id) DO UPDATE SET product_name = EXCLUDED.product_name, base_price = EXCLUDED.base_price;