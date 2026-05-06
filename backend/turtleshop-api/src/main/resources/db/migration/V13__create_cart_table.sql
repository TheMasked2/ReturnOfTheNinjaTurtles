CREATE TABLE IF NOT EXISTS CART (
    cart_id INT PRIMARY KEY,
    customer_id UUID,
    status VARCHAR(50), -- active / converted / abandoned
    order_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_cart_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id)
);