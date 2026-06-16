package org.turtleshop.api.modules.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.auth.model.SystemRole;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SystemRoleAccess {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<SystemRole> roleMapper = (rs, rowNum) -> SystemRole.builder()
            .roleId(rs.getInt("role_id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .build();

    public List<String> findRoleNamesByCustomerEmail(String email) {
        String sql = """
            SELECT DISTINCT r.name
            FROM system_roles r
            JOIN user_system_roles ur ON r.role_id = ur.role_id
            JOIN customer c ON ur.customer_id = c.customer_id
            WHERE LOWER(c.email) = LOWER(:email)
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("email", email),
                String.class
        );
    }

    public List<String> findPermissionCodesByCustomerEmail(String email) {
        String sql = """
            SELECT DISTINCT p.code
            FROM permissions p
            JOIN role_permissions rp ON p.permission_id = rp.permission_id
            JOIN system_roles r ON rp.role_id = r.role_id
            JOIN user_system_roles ur ON r.role_id = ur.role_id
            JOIN customer c ON ur.customer_id = c.customer_id
            WHERE LOWER(c.email) = LOWER(:email)
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("email", email),
                String.class
        );
    }
}