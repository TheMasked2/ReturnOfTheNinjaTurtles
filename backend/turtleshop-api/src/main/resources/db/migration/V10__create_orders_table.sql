CREATE TABLE IF NOT EXISTS ORDERS (
    order_id SERIAL PRIMARY KEY,
    customer_id UUID,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    total_amount DECIMAL(12, 2),
    shipping_address TEXT,
    shipped_date TIMESTAMP,
    delivery_status VARCHAR(50),
    delivered_date TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
);