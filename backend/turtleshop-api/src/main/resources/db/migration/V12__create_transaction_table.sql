CREATE TABLE IF NOT EXISTS TRANSACTION (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method_id INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
    CONSTRAINT fk_trans_method FOREIGN KEY (payment_method_id) REFERENCES PAYMENT_METHOD(payment_method_id)
);