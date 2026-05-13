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

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<SystemRole> roleMapper = (rs, rowNum) -> SystemRole.builder()
            .roleId(rs.getInt("role_id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .build();

    // Find Role by the Email of a Customer
    public List<String> findRoleNamesByCustomerEmail(String email) {
        String sql = """
            SELECT r.name FROM SYSTEM_ROLES r
            JOIN USER_SYSTEM_ROLES ur ON r.role_id = ur.role_id
            JOIN CUSTOMER c ON ur.customer_id = c.customer_id
            WHERE c.email = :email
        """;
        return jdbc.queryForList(sql, new MapSqlParameterSource("email", email), String.class);
    }

    // HELPER: Converts a Java object into list of values SQL understands
    private MapSqlParameterSource getParameters(SystemRole item) {
        return new MapSqlParameterSource()
                .addValue("name", item.getName())
                .addValue("description", item.getDescription());
    }
}