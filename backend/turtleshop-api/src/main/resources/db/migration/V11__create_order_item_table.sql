CREATE TABLE IF NOT EXISTS ORDER_ITEM (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT DEFAULT 1,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);