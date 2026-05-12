CREATE TABLE IF NOT EXISTS SHIPMENT (
    shipment_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    shipment_method VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    CONSTRAINT fk_shipment_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id)
);