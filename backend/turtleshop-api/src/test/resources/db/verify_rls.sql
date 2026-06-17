-- Proof of concept RLS verification using real seeded CUSTOMER and ORDERS data

# Be sure to grab an UUID from the Customers to start testing:
SELECT * FROM CUSTOMER ORDER BY customer_id LIMIT 5;

-- 1) Create a temporary restricted test role
CREATE ROLE rls_test_user NOLOGIN;
GRANT SELECT ON CUSTOMER TO rls_test_user;
GRANT SELECT ON ORDERS TO rls_test_user;

-- 2) Verify seeded data using a real customer UUID
BEGIN;
SET ROLE rls_test_user;

# This UUID will be different everytime the database gets seeded, be sure to change this before testing again.
SET LOCAL turtleshop.customer_id = '003e71fa-fea5-4b2d-81c4-9d39d8b90c36';

-- 3) Confirm session state and RLS metadata
SELECT
    current_setting('turtleshop.customer_id', true) AS current_customer,
    current_user,
    session_user;

SELECT relname, relrowsecurity, relforcerowsecurity
FROM pg_class
WHERE relname IN ('customer', 'orders');

SELECT schemaname, tablename, policyname, cmd, qual
FROM pg_policies
WHERE tablename IN ('customer', 'orders');

-- 4) Confirm customer isolation under the restricted role
# Should only diplay user's own customer record and orders associated with that customer_id
SELECT 'CUSTOMER' AS source_table, * FROM CUSTOMER ORDER BY customer_id;
SELECT 'ORDERS' AS source_table, * FROM ORDERS ORDER BY order_id;

RESET ROLE;
ROLLBACK;

REVOKE SELECT ON CUSTOMER FROM rls_test_user;
REVOKE SELECT ON ORDERS FROM rls_test_user;
DROP ROLE IF EXISTS rls_test_user;