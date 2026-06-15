CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id)
    REFERENCES system_roles(role_id)
    ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id)
    REFERENCES permissions(permission_id)
    ON DELETE CASCADE
    );