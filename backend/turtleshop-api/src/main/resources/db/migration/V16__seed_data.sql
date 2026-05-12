-- V16__seed_data.sql
-- Seed sample data for development/testing.

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
  (1, 'Visa', 'Credit Card'),
  (2, 'Mastercard', 'Credit Card'),
  (3, 'American Express', 'Credit Card'),
  (4, 'PayPal', 'Online Wallet'),
  (5, 'Apple Pay', 'Mobile Wallet'),
  (6, 'Google Pay', 'Mobile Wallet'),
  (7, 'Bank Transfer', 'Bank'),
  (8, 'Cash', 'Cash'),
  (9, 'Discover', 'Credit Card'),
  (10, 'CryptoPay', 'Crypto')
ON CONFLICT (payment_method_id) DO NOTHING;

-- 5) Customers
INSERT INTO CUSTOMER (customer_id, email, password, first_name, last_name, phone, address, city, postal_code, country) VALUES
  ('00000000-0000-0000-0000-000000000001', 'leonardo@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Leonardo', 'Nash', '555-0101', '1 Turtle Way', 'New York', '10001', 'USA'),
  ('00000000-0000-0000-0000-000000000002', 'raphael@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Raphael', 'Stone', '555-0102', '2 Turtle Way', 'Los Angeles', '90001', 'USA'),
  ('00000000-0000-0000-0000-000000000003', 'donatello@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Donatello', 'Lee', '555-0103', '3 Turtle Way', 'Chicago', '60601', 'USA'),
  ('00000000-0000-0000-0000-000000000004', 'michelangelo@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Michelangelo', 'Reed', '555-0104', '4 Turtle Way', 'Houston', '77001', 'USA'),
  ('00000000-0000-0000-0000-000000000005', 'casey@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Casey', 'Jones', '555-0105', '5 Turtle Way', 'Philadelphia', '19019', 'USA'),
  ('00000000-0000-0000-0000-000000000006', 'april@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'April', 'O''Neil', '555-0106', '6 Turtle Way', 'San Francisco', '94101', 'USA'),
  ('00000000-0000-0000-0000-000000000007', 'splinter@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Hamato', 'Yoshi', '555-0107', '7 Turtle Way', 'Seattle', '98101', 'USA'),
  ('00000000-0000-0000-0000-000000000008', 'shredder@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Oroku', 'Saki', '555-0108', '8 Turtle Way', 'Boston', '02101', 'USA'),
  ('00000000-0000-0000-0000-000000000009', 'karai@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Karai', 'Shin', '555-0109', '9 Turtle Way', 'Denver', '80201', 'USA'),
  ('00000000-0000-0000-0000-000000000010', 'otaku@example.com', '$2a$10$u8wzQydvTGa1K0vErAZGYuJ9fFJcgK.ET2y2/BQXJpMWpVrF2J7Qm', 'Otaku', 'Fan', '555-0110', '10 Turtle Way', 'Austin', '73301', 'USA')
ON CONFLICT (email) DO NOTHING;

-- 6) Inventory
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

-- 7) Product categories
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

-- 8) Wishlists
INSERT INTO WISHLIST (wishlist_id, customer_id) VALUES
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

-- 9) Wishlist items
INSERT INTO WISHLIST_ITEM (wishlist_item_id, wishlist_id, product_id) VALUES
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

-- 10) Carts
INSERT INTO CART (cart_id, customer_id, status, order_id) VALUES
  (1, '00000000-0000-0000-0000-000000000001', 'active', NULL),
  (2, '00000000-0000-0000-0000-000000000002', 'active', NULL),
  (3, '00000000-0000-0000-0000-000000000003', 'active', NULL),
  (4, '00000000-0000-0000-0000-000000000004', 'active', NULL),
  (5, '00000000-0000-0000-0000-000000000005', 'active', NULL),
  (6, '00000000-0000-0000-0000-000000000006', 'abandoned', NULL),
  (7, '00000000-0000-0000-0000-000000000007', 'abandoned', NULL),
  (8, '00000000-0000-0000-0000-000000000008', 'active', NULL),
  (9, '00000000-0000-0000-0000-000000000009', 'active', NULL),
  (10, '00000000-0000-0000-0000-000000000010', 'active', NULL)
ON CONFLICT (cart_id) DO NOTHING;

-- 11) Cart items
INSERT INTO CART_ITEM (cart_item_id, cart_id, product_id, quantity) VALUES
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

-- 12) Customer roles
INSERT INTO USER_SYSTEM_ROLES (customer_id, role_id) VALUES
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

-- 13) Resynchronize serial sequences
SELECT setval(pg_get_serial_sequence('product', 'product_id'),
              COALESCE((SELECT MAX(product_id) FROM product), 1), true);
SELECT setval(pg_get_serial_sequence('payment_method', 'payment_method_id'),
              COALESCE((SELECT MAX(payment_method_id) FROM payment_method), 1), true);
SELECT setval(pg_get_serial_sequence('inventory', 'inventory_id'),
              COALESCE((SELECT MAX(inventory_id) FROM inventory), 1), true);
SELECT setval(pg_get_serial_sequence('wishlist', 'wishlist_id'),
              COALESCE((SELECT MAX(wishlist_id) FROM wishlist), 1), true);
SELECT setval(pg_get_serial_sequence('cart', 'cart_id'),
              COALESCE((SELECT MAX(cart_id) FROM cart), 1), true);
SELECT setval(pg_get_serial_sequence('cart_item', 'cart_item_id'),
              COALESCE((SELECT MAX(cart_item_id) FROM cart_item), 1), true);
SELECT setval(pg_get_serial_sequence('wishlist_item', 'wishlist_item_id'),
              COALESCE((SELECT MAX(wishlist_item_id) FROM wishlist_item), 1), true);