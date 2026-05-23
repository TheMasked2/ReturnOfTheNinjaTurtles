INSERT INTO permissions (code, resource, action, scope, description) VALUES
-- PRODUCT / CATEGORY / INVENTORY
('PRODUCT_READ_ALL', 'PRODUCT', 'READ', 'ALL', 'Can view products'),
('PRODUCT_CREATE_ALL', 'PRODUCT', 'CREATE', 'ALL', 'Can create products'),
('PRODUCT_UPDATE_ALL', 'PRODUCT', 'UPDATE', 'ALL', 'Can update products'),
('PRODUCT_DELETE_ALL', 'PRODUCT', 'DELETE', 'ALL', 'Can delete products'),

('CATEGORY_READ_ALL', 'CATEGORY', 'READ', 'ALL', 'Can view categories'),
('CATEGORY_CREATE_ALL', 'CATEGORY', 'CREATE', 'ALL', 'Can create categories'),
('CATEGORY_UPDATE_ALL', 'CATEGORY', 'UPDATE', 'ALL', 'Can update categories'),
('CATEGORY_DELETE_ALL', 'CATEGORY', 'DELETE', 'ALL', 'Can delete categories'),

-- INVENTORY
('INVENTORY_READ_ALL', 'INVENTORY', 'READ', 'ALL', 'Can view inventory'),
('INVENTORY_UPDATE_ALL', 'INVENTORY', 'UPDATE', 'ALL', 'Can update inventory'),
('INVENTORY_CREATE_ALL', 'INVENTORY', 'CREATE', 'ALL', 'Can create inventory records'),
('INVENTORY_DELETE_ALL', 'INVENTORY', 'DELETE', 'ALL', 'Can delete inventory records'),
('INVENTORY_RESERVE_ALL', 'INVENTORY', 'MANAGE', 'ALL', 'Can reserve, consume, release, and restock inventory'),

-- CUSTOMER
('CUSTOMER_READ_OWN', 'CUSTOMER', 'READ', 'OWN', 'Can view own profile'),
('CUSTOMER_UPDATE_OWN', 'CUSTOMER', 'UPDATE', 'OWN', 'Can update own profile'),

('CUSTOMER_READ_ALL', 'CUSTOMER', 'READ', 'ALL', 'Can view all customers'),
('CUSTOMER_UPDATE_ALL', 'CUSTOMER', 'UPDATE', 'ALL', 'Can update all customers'),
('CUSTOMER_DELETE_ALL', 'CUSTOMER', 'DELETE', 'ALL', 'Can delete customers'),

-- CART / CART_ITEM
('CART_CREATE_OWN', 'CART', 'CREATE', 'OWN', 'Can create own cart'),
('CART_READ_OWN', 'CART', 'READ', 'OWN', 'Can view own cart'),
('CART_UPDATE_OWN', 'CART', 'UPDATE', 'OWN', 'Can update own cart'),
('CART_DELETE_OWN', 'CART', 'DELETE', 'OWN', 'Can delete own cart'),

('CART_CREATE_ALL', 'CART', 'CREATE', 'ALL', 'Can create carts for any customer'),
('CART_READ_ALL', 'CART', 'READ', 'ALL', 'Can view all carts'),
('CART_UPDATE_ALL', 'CART', 'UPDATE', 'ALL', 'Can update all carts'),
('CART_DELETE_ALL', 'CART', 'DELETE', 'ALL', 'Can delete all carts'),

-- ORDER / ORDER_ITEM
('ORDER_CREATE_OWN', 'ORDER', 'CREATE', 'OWN', 'Can create own orders'),
('ORDER_READ_OWN', 'ORDER', 'READ', 'OWN', 'Can view own orders'),
('ORDER_UPDATE_OWN', 'ORDER', 'UPDATE', 'OWN', 'Can update/cancel own orders'),

('ORDER_READ_ALL', 'ORDER', 'READ', 'ALL', 'Can view all orders'),
('ORDER_UPDATE_ALL', 'ORDER', 'UPDATE', 'ALL', 'Can update all orders'),
('ORDER_DELETE_ALL', 'ORDER', 'DELETE', 'ALL', 'Can delete orders'),

-- PAYMENT / TRANSACTION / PAYMENT_METHOD
('PAYMENT_CREATE_OWN', 'PAYMENT', 'CREATE', 'OWN', 'Can create own payments'),
('PAYMENT_READ_OWN', 'PAYMENT', 'READ', 'OWN', 'Can view own payments'),

('PAYMENT_READ_ALL', 'PAYMENT', 'READ', 'ALL', 'Can view all payments'),
('PAYMENT_UPDATE_ALL', 'PAYMENT', 'UPDATE', 'ALL', 'Can update payment records'),

-- SHIPMENT / SHIPMENT_STATUS_LOG
('SHIPMENT_READ_OWN', 'SHIPMENT', 'READ', 'OWN', 'Can view own shipments'),

('SHIPMENT_CREATE_ALL', 'SHIPMENT', 'CREATE', 'ALL', 'Can create shipments'),
('SHIPMENT_READ_ALL', 'SHIPMENT', 'READ', 'ALL', 'Can view all shipments'),
('SHIPMENT_UPDATE_ALL', 'SHIPMENT', 'UPDATE', 'ALL', 'Can update shipments'),
('SHIPMENT_DELETE_ALL', 'SHIPMENT', 'DELETE', 'ALL', 'Can delete shipments'),

-- WISHLIST / WISHLIST_ITEM
('WISHLIST_CREATE_OWN', 'WISHLIST', 'CREATE', 'OWN', 'Can create own wishlist'),
('WISHLIST_READ_OWN', 'WISHLIST', 'READ', 'OWN', 'Can view own wishlist'),
('WISHLIST_UPDATE_OWN', 'WISHLIST', 'UPDATE', 'OWN', 'Can update own wishlist'),
('WISHLIST_DELETE_OWN', 'WISHLIST', 'DELETE', 'OWN', 'Can delete own wishlist'),

('WISHLIST_CREATE_ALL', 'WISHLIST', 'CREATE', 'ALL', 'Can create wishlists for any customer'),
('WISHLIST_READ_ALL', 'WISHLIST', 'READ', 'ALL', 'Can view all wishlists'),
('WISHLIST_UPDATE_ALL', 'WISHLIST', 'UPDATE', 'ALL', 'Can update all wishlists'),
('WISHLIST_DELETE_ALL', 'WISHLIST', 'DELETE', 'ALL', 'Can delete all wishlists'),

-- REVIEW
('REVIEW_CREATE_OWN', 'REVIEW', 'CREATE', 'OWN', 'Can create own reviews'),
('REVIEW_READ_ALL', 'REVIEW', 'READ', 'ALL', 'Can view all reviews'),
('REVIEW_UPDATE_OWN', 'REVIEW', 'UPDATE', 'OWN', 'Can update own reviews'),
('REVIEW_DELETE_OWN', 'REVIEW', 'DELETE', 'OWN', 'Can delete own reviews'),

('REVIEW_DELETE_ALL', 'REVIEW', 'DELETE', 'ALL', 'Can delete any review'),

-- ROLE / PERMISSION MANAGEMENT
('ROLE_READ_ALL', 'ROLE', 'READ', 'ALL', 'Can view roles'),
('ROLE_ASSIGN_ALL', 'ROLE', 'MANAGE', 'ALL', 'Can assign roles'),
('ROLE_UPDATE_ALL', 'ROLE', 'UPDATE', 'ALL', 'Can update roles'),

('PERMISSION_READ_ALL', 'PERMISSION', 'READ', 'ALL', 'Can view permissions'),
('PERMISSION_ASSIGN_ALL', 'PERMISSION', 'MANAGE', 'ALL', 'Can assign permissions'),

-- AUDIT LOG
('AUDITLOG_READ_ALL', 'AUDITLOG', 'READ', 'ALL', 'Can view audit logs')
    ON CONFLICT (code) DO NOTHING;