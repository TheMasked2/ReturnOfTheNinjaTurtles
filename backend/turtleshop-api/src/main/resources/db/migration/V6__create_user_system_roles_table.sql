CREATE TABLE IF NOT EXISTS USER_SYSTEM_ROLES (
    customer_id INT,
    role_id INT,
    PRIMARY KEY (customer_id, role_id),
    CONSTRAINT fk_user_role_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES SYSTEM_ROLES(role_id)
);

-- Seed Data
INSERT INTO SYSTEM_ROLES (name, description) VALUES
('ROLE_USER', 'Standard customer access'),
('ROLE_ADMIN', 'Administrator access')
ON CONFLICT (name) DO NOTHING;