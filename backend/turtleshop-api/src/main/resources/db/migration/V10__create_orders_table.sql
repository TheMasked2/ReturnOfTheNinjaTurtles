CREATE TABLE IF NOT EXISTS ORDERS (
    order_id SERIAL PRIMARY KEY,
    customer_id UUID,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    total_amount DECIMAL(12, 2),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
);