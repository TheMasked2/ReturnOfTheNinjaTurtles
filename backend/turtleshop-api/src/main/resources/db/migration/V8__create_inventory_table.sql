CREATE TABLE IF NOT EXISTS INVENTORY (
    inventory_id INT PRIMARY KEY,
    product_id INT,
    quantity_available INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);