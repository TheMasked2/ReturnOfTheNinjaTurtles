CREATE TABLE IF NOT EXISTS TRANSACTION (
    transaction_id INT PRIMARY KEY,
    order_id INT,
    payment_method_id INT,
    external_reference VARCHAR(255),
    amount DECIMAL(12, 2),
    status VARCHAR(50), 
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    CONSTRAINT fk_trans_method FOREIGN KEY (payment_method_id) REFERENCES PAYMENT_METHOD(payment_method_id)
);