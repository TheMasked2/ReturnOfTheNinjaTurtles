-- Simple admin/reporting view for orders.
CREATE OR REPLACE VIEW v_order_summary AS
SELECT
    o.order_id,
    o.customer_id,
    c.email AS customer_email,
    o.order_date,
    o.status,
    o.total_amount,
    COUNT(oi.order_item_id) AS item_lines,
    COALESCE(SUM(oi.quantity), 0) AS total_items
FROM orders o
         JOIN customer c ON c.customer_id = o.customer_id
         LEFT JOIN order_item oi ON oi.order_id = o.order_id
GROUP BY
    o.order_id,
    o.customer_id,
    c.email,
    o.order_date,
    o.status,
    o.total_amount;


-- Simple product inventory view.
CREATE OR REPLACE VIEW v_product_inventory AS
SELECT
    p.product_id,
    p.product_name,
    p.base_price,
    i.quantity_available,
    i.quantity_reserved,
    i.quantity_available - i.quantity_reserved AS quantity_sellable,
    STRING_AGG(cat.name, ', ' ORDER BY cat.name) AS categories
FROM product p
         LEFT JOIN inventory i ON i.product_id = p.product_id
         LEFT JOIN product_category pc ON pc.product_id = p.product_id
         LEFT JOIN category cat ON cat.category_id = pc.category_id
GROUP BY
    p.product_id,
    p.product_name,
    p.base_price,
    i.quantity_available,
    i.quantity_reserved;