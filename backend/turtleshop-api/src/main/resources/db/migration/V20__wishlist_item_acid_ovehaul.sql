ALTER TABLE WISHLIST_ITEM
    ALTER COLUMN wishlist_id SET NOT NULL,
    ALTER COLUMN product_id SET NOT NULL,
    ALTER COLUMN added_at SET NOT NULL;

ALTER TABLE WISHLIST_ITEM
    ADD CONSTRAINT uq_wishlist_item_wishlist_product
    UNIQUE (wishlist_id, product_id);