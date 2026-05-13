package org.turtleshop.api.modules.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.auth.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerAccess {

    private final NamedParameterJdbcTemplate jdbc;

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<Customer> customerMapper = (rs, rowNum) -> Customer.builder()
            .customerId(rs.getObject("customer_id", java.util.UUID.class))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .phone(rs.getString("phone"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    // Get Customer by ID
    public Optional<Customer> getById(UUID id) {
        String sql = "SELECT * FROM CUSTOMER WHERE customer_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", id), customerMapper)
                .stream().findFirst();
    }

    // Get All Customers
    public List<Customer> getAll() {
        return jdbc.query("SELECT * FROM CUSTOMER", customerMapper);
    }

    // Find customer by email
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM CUSTOMER WHERE LOWER(email) = LOWER(:email)";
        return jdbc.query(sql, new MapSqlParameterSource("email", email), customerMapper)
                .stream().findFirst();
    }

    // Check if exists by email
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM CUSTOMER WHERE LOWER(email) = LOWER(:email)";
        Integer count = jdbc.queryForObject(sql, new MapSqlParameterSource("email", email), Integer.class);
        return count != null && count > 0;
    }

    // Insert and return Customer ID
    public UUID insertAndReturnId(Customer item) {
        UUID newId = UUID.randomUUID();

        String sql = """
        INSERT INTO CUSTOMER (customer_id, email, password, first_name, last_name, phone, created_at)
        VALUES (:id, :email, :password, :firstName, :lastName, :phone, NOW())
        """;

        MapSqlParameterSource params = getParameters(item)
                .addValue("id", newId);

        jdbc.update(sql, params);
        return newId;
    }

    // Adding the Role to the Customer
    public void addRoleToCustomer(UUID customerId, String roleName) {
        String sql = """
            INSERT INTO USER_SYSTEM_ROLES (customer_id, role_id)
            VALUES (:customerId, (SELECT role_id FROM SYSTEM_ROLES WHERE name = :roleName))
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("roleName", roleName);

        jdbc.update(sql, params);
    }

    // HELPER: Converts a Java object into list of values SQL understands
    private MapSqlParameterSource getParameters(Customer item) {
        return new MapSqlParameterSource()
                .addValue("email", item.getEmail())
                .addValue("password", item.getPassword())
                .addValue("firstName", item.getFirstName())
                .addValue("lastName", item.getLastName())
                .addValue("phone", item.getPhone());
    }

    // Test Connection
    public boolean testConnection() {
        try {
            jdbc.getJdbcTemplate().execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}