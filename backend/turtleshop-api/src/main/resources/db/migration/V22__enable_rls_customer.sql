ALTER TABLE CUSTOMER ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS customer_self_view ON CUSTOMER;
CREATE POLICY customer_self_view ON CUSTOMER
    FOR SELECT
    TO PUBLIC
    USING (
        current_setting('turtleshop.customer_id', true) IS NOT NULL
        AND customer_id = current_setting('turtleshop.customer_id', true)::UUID
    );

ALTER TABLE CUSTOMER FORCE ROW LEVEL SECURITY;