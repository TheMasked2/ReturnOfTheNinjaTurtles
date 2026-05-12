CREATE TABLE IF NOT EXISTS PRODUCT (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255),
    base_price DECIMAL(12, 2)
);