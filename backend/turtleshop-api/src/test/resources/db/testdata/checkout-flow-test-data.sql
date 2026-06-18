-- checkout-flow-test-data.sql
-- Seed sample data for checkout/payment/shipment integration and E2E tests.

-- ============================================================
-- 0) Clean previous checkout-flow test data
-- ============================================================

-- Delete in reverse dependency order to avoid foreign-key problems.
DELETE FROM SHIPMENT_STATUS_LOG;
DELETE FROM SHIPMENT;
DELETE FROM TRANSACTION;
DELETE FROM ORDER_ITEM;
DELETE FROM CART_ITEM;
DELETE FROM CART;
DELETE FROM ORDERS;

DELETE FROM USER_SYSTEM_ROLES
WHERE customer_id IN (
                      '00000000-0000-0000-0000-000000000001',
                      '00000000-0000-0000-0000-000000000002',
                      '00000000-0000-0000-0000-000000000003',
                      '00000000-0000-0000-0000-000000000004',
                      '00000000-0000-0000-0000-000000000005',
                      '00000000-0000-0000-0000-000000000006',
                      '00000000-0000-0000-0000-000000000007',
                      '00000000-0000-0000-0000-000000000008',
                      '00000000-0000-0000-0000-000000000009',
                      '00000000-0000-0000-0000-000000000010'
    );

DELETE FROM WISHLIST_ITEM;
DELETE FROM WISHLIST;

-- Explicitly remove sensitive rows first.
-- ON DELETE CASCADE would also remove them when CUSTOMER is deleted,
-- but explicitly listing them makes the test cleanup clearer.
DELETE FROM CUSTOMER_SENSITIVE_DATA
WHERE customer_id IN (
                      '00000000-0000-0000-0000-000000000001',
                      '00000000-0000-0000-0000-000000000002',
                      '00000000-0000-0000-0000-000000000003',
                      '00000000-0000-0000-0000-000000000004',
                      '00000000-0000-0000-0000-000000000005',
                      '00000000-0000-0000-0000-000000000006',
                      '00000000-0000-0000-0000-000000000007',
                      '00000000-0000-0000-0000-000000000008',
                      '00000000-0000-0000-0000-000000000009',
                      '00000000-0000-0000-0000-000000000010'
    );

DELETE FROM CUSTOMER
WHERE customer_id IN (
                      '00000000-0000-0000-0000-000000000001',
                      '00000000-0000-0000-0000-000000000002',
                      '00000000-0000-0000-0000-000000000003',
                      '00000000-0000-0000-0000-000000000004',
                      '00000000-0000-0000-0000-000000000005',
                      '00000000-0000-0000-0000-000000000006',
                      '00000000-0000-0000-0000-000000000007',
                      '00000000-0000-0000-0000-000000000008',
                      '00000000-0000-0000-0000-000000000009',
                      '00000000-0000-0000-0000-000000000010'
    );

-- ============================================================
-- 1) Ensure roles exist
-- ============================================================

-- V6 already seeds these roles, but keeping this makes the test
-- data independent and repeatable.
INSERT INTO SYSTEM_ROLES (
    name,
    description
)
VALUES
    ('ROLE_USER', 'Standard customer access'),
    ('ROLE_ADMIN', 'Administrator access')
    ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- 2) Customers: non-sensitive information
-- ============================================================

INSERT INTO CUSTOMER (
    customer_id,
    email,
    password_hash,
    first_name,
    last_name
)
VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        'leonardo@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Leonardo',
        'Nash'
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'raphael@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Raphael',
        'Stone'
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'donatello@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Donatello',
        'Lee'
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        'michelangelo@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Michelangelo',
        'Reed'
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        'casey@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Casey',
        'Jones'
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        'april@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'April',
        'O''Neil'
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        'splinter@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Hamato',
        'Yoshi'
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        'shredder@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Oroku',
        'Saki'
    ),
    (
        '00000000-0000-0000-0000-000000000009',
        'karai@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Karai',
        'Shin'
    ),
    (
        '00000000-0000-0000-0000-000000000010',
        'otaku@example.com',
        '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm',
        'Otaku',
        'Fan'
    )
    ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 3) Customer sensitive information
-- ============================================================

INSERT INTO CUSTOMER_SENSITIVE_DATA (
    customer_id,
    phone,
    address,
    city,
    postal_code,
    country
)
VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        '555-0101',
        '1 Turtle Way',
        'New York',
        '10001',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        '555-0102',
        '2 Turtle Way',
        'Los Angeles',
        '90001',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        '555-0103',
        '3 Turtle Way',
        'Chicago',
        '60601',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        '555-0104',
        '4 Turtle Way',
        'Houston',
        '77001',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        '555-0105',
        '5 Turtle Way',
        'Philadelphia',
        '19019',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        '555-0106',
        '6 Turtle Way',
        'San Francisco',
        '94101',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        '555-0107',
        '7 Turtle Way',
        'Seattle',
        '98101',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        '555-0108',
        '8 Turtle Way',
        'Boston',
        '02101',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000009',
        '555-0109',
        '9 Turtle Way',
        'Denver',
        '80201',
        'USA'
    ),
    (
        '00000000-0000-0000-0000-000000000010',
        '555-0110',
        '10 Turtle Way',
        'Austin',
        '73301',
        'USA'
    )
    ON CONFLICT (customer_id) DO UPDATE
                                     SET
                                         phone = EXCLUDED.phone,
                                     address = EXCLUDED.address,
                                     city = EXCLUDED.city,
                                     postal_code = EXCLUDED.postal_code,
                                     country = EXCLUDED.country,
                                     updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- 4) Inventory
-- ============================================================

-- Inventory is seeded by normal Flyway migrations.
-- For checkout tests, make product 1 predictable.
UPDATE INVENTORY
SET
    quantity_available = 20,
    quantity_reserved = 0
WHERE product_id = 1;

-- ============================================================
-- 5) Product categories
-- ============================================================

-- Product categories are seeded by normal Flyway migrations.
-- They are not repeated here.

-- ============================================================
-- 6) Wishlists
-- ============================================================

INSERT INTO WISHLIST (
    wishlist_id,
    customer_id
)
VALUES
    (1, '00000000-0000-0000-0000-000000000001'),
    (2, '00000000-0000-0000-0000-000000000002'),
    (3, '00000000-0000-0000-0000-000000000003'),
    (4, '00000000-0000-0000-0000-000000000004'),
    (5, '00000000-0000-0000-0000-000000000005'),
    (6, '00000000-0000-0000-0000-000000000006'),
    (7, '00000000-0000-0000-0000-000000000007'),
    (8, '00000000-0000-0000-0000-000000000008'),
    (9, '00000000-0000-0000-0000-000000000009'),
    (10, '00000000-0000-0000-0000-000000000010')
    ON CONFLICT (wishlist_id) DO NOTHING;

-- ============================================================
-- 7) Wishlist items
-- ============================================================

INSERT INTO WISHLIST_ITEM (
    wishlist_item_id,
    wishlist_id,
    product_id
)
VALUES
    (1, 1, 1),
    (2, 1, 6),
    (3, 2, 2),
    (4, 3, 3),
    (5, 4, 4),
    (6, 5, 5),
    (7, 6, 7),
    (8, 7, 8),
    (9, 8, 9),
    (10, 9, 10)
    ON CONFLICT (wishlist_item_id) DO NOTHING;

-- ============================================================
-- 8) Carts
-- ============================================================

INSERT INTO CART (
    cart_id,
    customer_id,
    status,
    order_id
)
VALUES
    (1, '00000000-0000-0000-0000-000000000001', 'ACTIVE', NULL),
    (2, '00000000-0000-0000-0000-000000000002', 'ACTIVE', NULL),
    (3, '00000000-0000-0000-0000-000000000003', 'ACTIVE', NULL),
    (4, '00000000-0000-0000-0000-000000000004', 'ACTIVE', NULL),
    (5, '00000000-0000-0000-0000-000000000005', 'ACTIVE', NULL),
    (6, '00000000-0000-0000-0000-000000000006', 'ABANDONED', NULL),
    (7, '00000000-0000-0000-0000-000000000007', 'ABANDONED', NULL),
    (8, '00000000-0000-0000-0000-000000000008', 'ACTIVE', NULL),
    (9, '00000000-0000-0000-0000-000000000009', 'ACTIVE', NULL),
    (10, '00000000-0000-0000-0000-000000000010', 'ACTIVE', NULL)
    ON CONFLICT (cart_id) DO NOTHING;

-- ============================================================
-- 9) Cart items
-- ============================================================

INSERT INTO CART_ITEM (
    cart_item_id,
    cart_id,
    product_id,
    quantity
)
VALUES
    (1, 1, 1, 2),
    (2, 2, 2, 1),
    (3, 3, 3, 1),
    (4, 4, 4, 2),
    (5, 5, 5, 1),
    (6, 6, 6, 3),
    (7, 7, 7, 1),
    (8, 8, 8, 2),
    (9, 9, 9, 1),
    (10, 10, 10, 2)
    ON CONFLICT (cart_item_id) DO NOTHING;

-- ============================================================
-- 10) Customer roles
-- ============================================================

INSERT INTO USER_SYSTEM_ROLES (
    customer_id,
    role_id
)
VALUES
    ('00000000-0000-0000-0000-000000000001', 1),
    ('00000000-0000-0000-0000-000000000002', 1),
    ('00000000-0000-0000-0000-000000000003', 1),
    ('00000000-0000-0000-0000-000000000004', 1),
    ('00000000-0000-0000-0000-000000000005', 1),
    ('00000000-0000-0000-0000-000000000006', 1),
    ('00000000-0000-0000-0000-000000000007', 1),
    ('00000000-0000-0000-0000-000000000008', 1),
    ('00000000-0000-0000-0000-000000000009', 1),
    ('00000000-0000-0000-0000-000000000010', 1)
    ON CONFLICT (customer_id, role_id) DO NOTHING;

-- ============================================================
-- 11) Resynchronize serial sequences
-- ============================================================

SELECT setval(
               pg_get_serial_sequence('wishlist', 'wishlist_id'),
               COALESCE((SELECT MAX(wishlist_id) FROM wishlist), 1),
               true
       );

SELECT setval(
               pg_get_serial_sequence('cart', 'cart_id'),
               COALESCE((SELECT MAX(cart_id) FROM cart), 1),
               true
       );

SELECT setval(
               pg_get_serial_sequence('cart_item', 'cart_item_id'),
               COALESCE((SELECT MAX(cart_item_id) FROM cart_item), 1),
               true
       );

SELECT setval(
               pg_get_serial_sequence('wishlist_item', 'wishlist_item_id'),
               COALESCE((SELECT MAX(wishlist_item_id) FROM wishlist_item), 1),
               true
       );