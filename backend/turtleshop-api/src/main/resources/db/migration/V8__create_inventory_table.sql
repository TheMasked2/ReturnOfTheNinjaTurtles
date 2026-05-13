CREATE TABLE IF NOT EXISTS INVENTORY (
    inventory_id SERIAL PRIMARY KEY,
    product_id INT UNIQUE,
    quantity_available INT DEFAULT 0,
    quantity_reserved INT DEFAULT 0,
    version TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);