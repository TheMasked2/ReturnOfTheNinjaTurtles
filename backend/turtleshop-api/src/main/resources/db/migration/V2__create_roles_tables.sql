CREATE TABLE IF NOT EXISTS SYSTEM_ROLES (
    role_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS USER_SYSTEM_ROLES (
    customer_id UUID REFERENCES CUSTOMER(customer_id),
    role_id INT REFERENCES SYSTEM_ROLES(role_id),
    PRIMARY KEY (customer_id, role_id)
);

-- Seed Data
INSERT INTO SYSTEM_ROLES (name, description) VALUES
('ROLE_USER', 'Standard customer access'),
('ROLE_ADMIN', 'Administrator access')
ON CONFLICT (name) DO NOTHING;