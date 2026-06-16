CREATE TABLE IF NOT EXISTS permissions (
    permission_id SERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT chk_permission_action
    CHECK (action IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'MANAGE')),

    CONSTRAINT chk_permission_scope
    CHECK (scope IN ('OWN', 'ALL'))
    );