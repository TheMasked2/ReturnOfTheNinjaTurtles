-- V27__split_customer_sensitive_data.sql
--
-- Splits sensitive customer data into a separate table and applies
-- the same customer_id-based RLS style already used by CUSTOMER and ORDERS.

-- Temporarily remove FORCE RLS so migration can move existing CUSTOMER rows.
ALTER TABLE CUSTOMER NO FORCE ROW LEVEL SECURITY;

CREATE TABLE CUSTOMER_SENSITIVE_DATA (
                                         customer_id UUID PRIMARY KEY,
                                         phone VARCHAR(50),
                                         address TEXT,
                                         city VARCHAR(100),
                                         postal_code VARCHAR(20),
                                         country VARCHAR(100),
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT fk_customer_sensitive_data_customer
                                             FOREIGN KEY (customer_id)
                                                 REFERENCES CUSTOMER(customer_id)
                                                 ON DELETE CASCADE
);

INSERT INTO CUSTOMER_SENSITIVE_DATA (
    customer_id,
    phone,
    address,
    city,
    postal_code,
    country,
    updated_at
)
SELECT
    customer_id,
    phone,
    address,
    city,
    postal_code,
    country,
    CURRENT_TIMESTAMP
FROM CUSTOMER;

ALTER TABLE CUSTOMER
DROP COLUMN phone,
    DROP COLUMN address,
    DROP COLUMN city,
    DROP COLUMN postal_code,
    DROP COLUMN country;

-- ALTER TABLE CUSTOMER
--     RENAME COLUMN password TO password_hash;

-- Re-enable FORCE RLS on CUSTOMER.
ALTER TABLE CUSTOMER FORCE ROW LEVEL SECURITY;

-- Apply the same RLS pattern to the sensitive table.
ALTER TABLE CUSTOMER_SENSITIVE_DATA ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS customer_sensitive_self_view ON CUSTOMER_SENSITIVE_DATA;
CREATE POLICY customer_sensitive_self_view ON CUSTOMER_SENSITIVE_DATA
    FOR SELECT
                        TO PUBLIC
                        USING (
                        current_setting('turtleshop.customer_id', true) IS NOT NULL
                        AND customer_id = current_setting('turtleshop.customer_id', true)::UUID
                        );

ALTER TABLE CUSTOMER_SENSITIVE_DATA FORCE ROW LEVEL SECURITY;