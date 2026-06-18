ALTER TABLE ORDERS ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS customer_self_view ON ORDERS;
CREATE POLICY customer_self_view ON ORDERS
    FOR SELECT
    TO PUBLIC
    USING (
        current_setting('turtleshop.customer_id', true) IS NOT NULL
        AND customer_id = current_setting('turtleshop.customer_id', true)::UUID
    );

ALTER TABLE ORDERS FORCE ROW LEVEL SECURITY;