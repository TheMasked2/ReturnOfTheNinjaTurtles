-- checkout-low-inventory-test-data.sql
-- Changes product 1 to a predictable low-inventory state.

UPDATE CART_ITEM
SET quantity = 2
WHERE cart_item_id = 1
  AND cart_id = 1
  AND product_id = 1;

UPDATE INVENTORY
SET
    quantity_available = 1,
    quantity_reserved = 0
WHERE product_id = 1;