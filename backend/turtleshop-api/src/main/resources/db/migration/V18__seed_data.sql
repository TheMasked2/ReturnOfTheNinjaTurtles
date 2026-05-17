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

-- 7) Resynchronize serial sequences
SELECT setval(pg_get_serial_sequence('product', 'product_id'),
              COALESCE((SELECT MAX(product_id) FROM product), 1), true);
SELECT setval(pg_get_serial_sequence('payment_method', 'payment_method_id'),
              COALESCE((SELECT MAX(payment_method_id) FROM payment_method), 1), true);
SELECT setval(pg_get_serial_sequence('inventory', 'inventory_id'),
              COALESCE((SELECT MAX(inventory_id) FROM inventory), 1), true);