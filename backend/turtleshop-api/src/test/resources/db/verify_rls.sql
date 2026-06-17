-- Proof of concept RLS verification using real seeded CUSTOMER and ORDERS data

-- 1) Create a temporary restricted test role
CREATE ROLE rls_test_user NOLOGIN;
GRANT SELECT ON CUSTOMER TO rls_test_user;
GRANT SELECT ON ORDERS TO rls_test_user;

-- 2) Verify seeded data using a real customer UUID
BEGIN;

SET ROLE rls_test_user;
-- This should match a seeded customer_id in the CUSTOMER table, probably different after a new seed.
SET LOCAL turtleshop.customer_id = 'a86f29f8-7130-4bac-b421-ef788860b083';

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
SELECT 'CUSTOMER' AS source_table, * FROM CUSTOMER ORDER BY customer_id;
SELECT 'ORDERS' AS source_table, * FROM ORDERS ORDER BY order_id;

-- Expected results:
-- - CUSTOMER should return only the seeded row for customer_id = (chosen uuid)
-- - ORDERS should return only rows with customer_id = (chosen uuid)

RESET ROLE;
ROLLBACK;

REVOKE SELECT ON CUSTOMER FROM rls_test_user;
REVOKE SELECT ON ORDERS FROM rls_test_user;

DROP ROLE IF EXISTS rls_test_user;