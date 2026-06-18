-- clean-test-data.sql
-- Removes mutable integration-test data and resets serial sequences.

TRUNCATE TABLE
    SHIPMENT_STATUS_LOG,
    SHIPMENT,
    TRANSACTION,
    ORDER_ITEM,
    CART_ITEM,
    CART,
    WISHLIST_ITEM,
    WISHLIST,
    USER_SYSTEM_ROLES,
    ORDERS,
    CUSTOMER_SENSITIVE_DATA,
    CUSTOMER
RESTART IDENTITY CASCADE;