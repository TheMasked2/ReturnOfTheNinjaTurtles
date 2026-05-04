CREATE TABLE IF NOT EXISTS WISHLIST (
    wishlist_id INT PRIMARY KEY,
    customer_id INT,
    CONSTRAINT fk_wishlist_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
);