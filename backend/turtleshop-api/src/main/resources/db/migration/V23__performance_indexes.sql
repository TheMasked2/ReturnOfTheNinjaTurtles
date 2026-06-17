-- Login / auth performance
CREATE INDEX IF NOT EXISTS ix_customer_email_lower
    ON customer (LOWER(email));

CREATE INDEX IF NOT EXISTS ix_user_system_roles_customer
    ON user_system_roles(customer_id);

CREATE INDEX IF NOT EXISTS ix_user_system_roles_role
    ON user_system_roles(role_id);

CREATE INDEX IF NOT EXISTS ix_orders_order_date_desc
    ON orders(order_date DESC);

CREATE INDEX IF NOT EXISTS ix_customer_created_at_id
    ON customer(created_at DESC, customer_id ASC);

-- Orders performance
CREATE INDEX IF NOT EXISTS ix_orders_customer_date
    ON orders(customer_id, order_date DESC);

CREATE INDEX IF NOT EXISTS ix_orders_status_date
    ON orders(status, order_date DESC);

-- Order items performance
CREATE INDEX IF NOT EXISTS ix_order_item_order
    ON order_item(order_id);

CREATE INDEX IF NOT EXISTS ix_order_item_product
    ON order_item(product_id);

-- Cart performance
CREATE INDEX IF NOT EXISTS ix_cart_customer_status
    ON cart(customer_id, status);

CREATE INDEX IF NOT EXISTS ix_cart_item_cart
    ON cart_item(cart_id);

CREATE INDEX IF NOT EXISTS ix_cart_item_product
    ON cart_item(product_id);

-- Inventory performance
CREATE INDEX IF NOT EXISTS ix_inventory_quantity_available
    ON inventory(quantity_available);

-- Wishlist performance
CREATE INDEX IF NOT EXISTS ix_wishlist_customer
    ON wishlist(customer_id);

CREATE INDEX IF NOT EXISTS ix_wishlist_item_wishlist_product
    ON wishlist_item(wishlist_id, product_id);

-- Product/category join performance
CREATE INDEX IF NOT EXISTS ix_product_category_category_product
    ON product_category(category_id, product_id);

-- Shipment tracking performance
CREATE INDEX IF NOT EXISTS ix_shipment_status_log_shipment_date
    ON shipment_status_log(shipment_id, status_change_date DESC);