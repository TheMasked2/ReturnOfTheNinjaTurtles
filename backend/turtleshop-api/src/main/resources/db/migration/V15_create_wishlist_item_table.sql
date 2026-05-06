CREATE TABLE IF NOT EXISTS WISHLIST_ITEM (
    wishlist_item_id INT PRIMARY KEY,
    wishlist_id INT,
    product_id INT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wish_item_list FOREIGN KEY (wishlist_id) REFERENCES WISHLIST(wishlist_id),
    CONSTRAINT fk_wish_item_prod FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);