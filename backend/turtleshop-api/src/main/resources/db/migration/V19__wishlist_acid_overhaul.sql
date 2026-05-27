ALTER TABLE WISHLIST
    ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE WISHLIST
    ADD CONSTRAINT uq_wishlist_customer
    UNIQUE (customer_id);

ALTER TABLE WISHLIST
    DROP CONSTRAINT fk_wishlist_customer,
    ADD CONSTRAINT fk_wishlist_customer
    FOREIGN KEY (customer_id)
    REFERENCES CUSTOMER(customer_id)
    ON DELETE RESTRICT;