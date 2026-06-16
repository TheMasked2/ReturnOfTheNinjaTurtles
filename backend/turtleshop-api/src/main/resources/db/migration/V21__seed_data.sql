-- V18__seed_data.sql
-- Seed sample data for development.

-- 1) Ensure roles exist (V6 already seeds these too)
INSERT INTO SYSTEM_ROLES (name, description) VALUES
 ('ROLE_USER', 'Standard customer access'),
 ('ROLE_ADMIN', 'Administrator access')
ON CONFLICT (name) DO NOTHING;

-- 2) Categories
INSERT INTO CATEGORY (category_id, name, description) VALUES
  (1, 'Comic Books', 'Collectible comics and graphic novels'),
  (2, 'Apparel', 'T-shirts, hoodies and clothing'),
  (3, 'Accessories', 'Hats, backpacks and wallets'),
  (4, 'Toys', 'Action figures and playsets'),
  (5, 'Home', 'Decor, mugs and posters'),
  (6, 'Media', 'DVDs, games and soundtracks'),
  (7, 'Gadgets', 'Tech accessories and small electronics'),
  (8, 'Books', 'Novels, art books and guides'),
  (9, 'Collectibles', 'Limited edition and display items'),
  (10, 'Fitness', 'Workout gear and gym accessories')
ON CONFLICT (category_id) DO NOTHING;

-- 3) Products
INSERT INTO PRODUCT (product_id, product_name, base_price) VALUES
    (1, 'Retro Vinyl Record', 24.99),
    (2, 'Studio Headphones', 79.99),
    (3, 'Portable Bluetooth Speaker', 39.99),
    (4, 'DJ Turntable', 129.99),
    (5, 'Cassette Tape Set', 19.99),
    (6, 'Guitar Effect Pedal', 54.99),
    (7, 'Record Cleaning Kit', 17.99),
    (8, 'Band Tour Poster', 14.99),
    (9, 'Wireless Earbuds', 59.99),
    (10, 'Music-themed Hoodie', 44.99)
ON CONFLICT (product_id) DO NOTHING;

-- 4) Payment methods
INSERT INTO PAYMENT_METHOD (payment_method_id, provider, type) VALUES
   (1, 'VISA/ Mastercard/ Amex', 'Credit Card'),
   (2, 'PayPal', 'Online Wallet'),
   (3, 'IDEAL/ Wero', 'Mobile banking')

ON CONFLICT (payment_method_id) DO NOTHING;

-- 5) Inventory
-- Demo inventory for development/manual Swagger testing.
-- Automated tests can override these values in their own test data files.
INSERT INTO INVENTORY (inventory_id, product_id, quantity_available, quantity_reserved) VALUES
    (1, 1, 20, 2),
    (2, 2, 30, 1),
    (3, 3, 15, 0),
    (4, 4, 40, 3),
    (5, 5, 12, 0),
    (6, 6, 50, 4),
    (7, 7, 25, 1),
    (8, 8, 18, 0),
    (9, 9, 60, 5),
    (10, 10, 22, 2)
ON CONFLICT (inventory_id) DO NOTHING;

-- 6) Product categories
INSERT INTO PRODUCT_CATEGORY (product_id, category_id) VALUES
   (1, 4),
   (2, 2),
   (3, 7),
   (4, 4),
   (5, 9),
   (6, 1),
   (7, 2),
   (8, 5),
   (9, 5),
   (10, 8)
ON CONFLICT (product_id, category_id) DO NOTHING;

-- 8) Assign the permissions to the user role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM system_roles r
JOIN permissions p ON p.code IN (
    -- Product/catalog access
    'PRODUCT_READ_ALL',
    'CATEGORY_READ_ALL',

    -- Own customer data
    'CUSTOMER_READ_OWN',
    'CUSTOMER_UPDATE_OWN',

    -- Own cart
    'CART_CREATE_OWN',
    'CART_READ_OWN',
    'CART_UPDATE_OWN',
    'CART_DELETE_OWN',

    -- Own orders
    'ORDER_CREATE_OWN',
    'ORDER_READ_OWN',
    'ORDER_UPDATE_OWN',

    -- Own payments
    'PAYMENT_CREATE_OWN',
    'PAYMENT_READ_OWN',
    'PAYMENT_READ_ALL', -- Allow users to see all payment methods for selection
    'PAYMENT_UPDATE_OWN',

    -- Own shipments
    'SHIPMENT_READ_OWN',

    -- Own wishlist
    'WISHLIST_CREATE_OWN',
    'WISHLIST_READ_OWN',
    'WISHLIST_UPDATE_OWN',
    'WISHLIST_DELETE_OWN',

    -- Reviews
    'REVIEW_CREATE_OWN',
    'REVIEW_READ_ALL',
    'REVIEW_UPDATE_OWN',
    'REVIEW_DELETE_OWN'
)
WHERE r.name = 'ROLE_USER'
    ON CONFLICT DO NOTHING;

-- 9) Assign the permissions to the admin role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM system_roles r
JOIN permissions p ON p.code IN (
    -- Product/catalog management
    'PRODUCT_READ_ALL',
    'PRODUCT_CREATE_ALL',
    'PRODUCT_UPDATE_ALL',
    'PRODUCT_DELETE_ALL',

    'CATEGORY_READ_ALL',
    'CATEGORY_CREATE_ALL',
    'CATEGORY_UPDATE_ALL',
    'CATEGORY_DELETE_ALL',

    'INVENTORY_READ_ALL',
    'INVENTORY_UPDATE_ALL',
    'INVENTORY_CREATE_ALL',
    'INVENTORY_DELETE_ALL',
    'INVENTORY_RESERVE_ALL',

    -- Customer management
    'CUSTOMER_READ_ALL',
    'CUSTOMER_UPDATE_ALL',
    'CUSTOMER_DELETE_ALL',

    -- Cart management
    'CART_CREATE_ALL',
    'CART_READ_ALL',
    'CART_UPDATE_ALL',
    'CART_DELETE_ALL',

    -- Order management
    'ORDER_READ_ALL',
    'ORDER_UPDATE_ALL',
    'ORDER_DELETE_ALL',

    -- Payment management
    'PAYMENT_READ_ALL',
    'PAYMENT_UPDATE_ALL',

    -- Shipment management
    'SHIPMENT_CREATE_ALL',
    'SHIPMENT_READ_ALL',
    'SHIPMENT_UPDATE_ALL',
    'SHIPMENT_DELETE_ALL',

    -- Wishlist moderation/viewing
    'WISHLIST_CREATE_ALL',
    'WISHLIST_READ_ALL',
    'WISHLIST_UPDATE_ALL',
    'WISHLIST_DELETE_ALL',

    -- Review moderation
    'REVIEW_READ_ALL',
    'REVIEW_DELETE_ALL',

    -- Role and permission management
    'ROLE_READ_ALL',
    'ROLE_ASSIGN_ALL',
    'ROLE_UPDATE_ALL',

    'PERMISSION_READ_ALL',
    'PERMISSION_ASSIGN_ALL',

    -- Audit log
    'AUDITLOG_READ_ALL'
)
WHERE r.name = 'ROLE_ADMIN'
    ON CONFLICT DO NOTHING;

-- 10) Resynchronize serial sequences
SELECT setval(pg_get_serial_sequence('product', 'product_id'),
              COALESCE((SELECT MAX(product_id) FROM product), 1), true);
SELECT setval(pg_get_serial_sequence('payment_method', 'payment_method_id'),
              COALESCE((SELECT MAX(payment_method_id) FROM payment_method), 1), true);
SELECT setval(pg_get_serial_sequence('inventory', 'inventory_id'),
              COALESCE((SELECT MAX(inventory_id) FROM inventory), 1), true);