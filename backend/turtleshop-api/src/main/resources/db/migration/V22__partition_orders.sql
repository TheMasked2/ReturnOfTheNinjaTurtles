-- Partitions the ORDERS table.

ALTER TABLE IF EXISTS order_item DROP CONSTRAINT IF EXISTS fk_item_order;
ALTER TABLE IF EXISTS "transaction" DROP CONSTRAINT IF EXISTS fk_trans_order;
ALTER TABLE IF EXISTS shipment DROP CONSTRAINT IF EXISTS fk_shipment_order;
ALTER TABLE IF EXISTS cart DROP CONSTRAINT IF EXISTS fk_cart_order;

ALTER SEQUENCE IF EXISTS orders_order_id_seq OWNED BY NONE;

ALTER TABLE IF EXISTS orders RENAME TO orders_old;
ALTER INDEX IF EXISTS orders_pkey RENAME TO orders_old_pkey;

CREATE TABLE orders (
                        order_id INT NOT NULL DEFAULT nextval('orders_order_id_seq'::regclass),
                        customer_id UUID,
                        order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50),
                        total_amount DECIMAL(12, 2),

                        CONSTRAINT orders_pkey PRIMARY KEY (order_id),
                        CONSTRAINT fk_order_customer
                            FOREIGN KEY (customer_id)
                                REFERENCES customer(customer_id)
) PARTITION BY HASH (order_id);

CREATE TABLE orders_p0 PARTITION OF orders
    FOR VALUES WITH (MODULUS 4, REMAINDER 0);

CREATE TABLE orders_p1 PARTITION OF orders
    FOR VALUES WITH (MODULUS 4, REMAINDER 1);

CREATE TABLE orders_p2 PARTITION OF orders
    FOR VALUES WITH (MODULUS 4, REMAINDER 2);

CREATE TABLE orders_p3 PARTITION OF orders
    FOR VALUES WITH (MODULUS 4, REMAINDER 3);

INSERT INTO orders (order_id, customer_id, order_date, status, total_amount)
SELECT order_id, customer_id, order_date, status, total_amount
FROM orders_old;

DROP TABLE orders_old;

ALTER SEQUENCE IF EXISTS orders_order_id_seq OWNED BY orders.order_id;

ALTER TABLE order_item
    ADD CONSTRAINT fk_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id);

ALTER TABLE "transaction"
    ADD CONSTRAINT fk_trans_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id);

ALTER TABLE shipment
    ADD CONSTRAINT fk_shipment_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id);

ALTER TABLE cart
    ADD CONSTRAINT fk_cart_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id);

SELECT setval(
               'orders_order_id_seq',
               COALESCE((SELECT MAX(order_id) FROM orders), 1),
               true
       );