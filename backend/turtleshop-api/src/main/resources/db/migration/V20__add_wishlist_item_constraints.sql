ALTER TABLE WISHLIST_ITEM
ADD CONSTRAINT uq_wishlist_item_wishlist_product
UNIQUE (wishlist_id, product_id);