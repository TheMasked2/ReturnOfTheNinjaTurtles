package org.turtleshop.api.modules.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.core.repository.IGenericAccess;
import org.turtleshop.api.modules.auth.model.Customer;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomerAccess implements IGenericAccess<Customer, Integer> {

    private static final String COL_ID = "customer_id";

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Customer> customerMapper = (rs, rowNum) -> Customer.builder()
            .customerId(rs.getInt(COL_ID))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .phone(rs.getString("phone"))
            .address(rs.getString("address"))
            .city(rs.getString("city"))
            .postalCode(rs.getString("postal_code"))
            .country(rs.getString("country"))
            .bank(rs.getString("bank"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    @Override
    public Optional<Customer> getById(Integer id) {
        String sql = "SELECT * FROM CUSTOMER WHERE " + COL_ID + " = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbc.query(sql, params, customerMapper).stream().findFirst();
    }

    @Override
    public List<Customer> getAll() {
        String sql = "SELECT * FROM CUSTOMER";
        return jdbc.query(sql, customerMapper);
    }

    @Override
    public void insert(Customer item) {
        String sql = """
            INSERT INTO CUSTOMER (email, password, first_name, last_name, phone, address, city, postal_code, country, bank, created_at)
            VALUES (:email, :password, :firstName, :lastName, :phone, :address, :city, :postalCode, :country, :bank, NOW())
        """;
        jdbc.update(sql, getParameters(item));
    }

    @Override
    public void update(Customer item) {
        String sql = "UPDATE CUSTOMER " +
                "SET email = :email, first_name = :firstName, last_name = :lastName " +
                "WHERE " + COL_ID + " = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", item.getEmail())
                .addValue("firstName", item.getFirstName())
                .addValue("lastName", item.getLastName())
                .addValue("id", item.getCustomerId());
        jdbc.update(sql, params);
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM CUSTOMER WHERE " + COL_ID + " = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM CUSTOMER WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource("email", email);
        return jdbc.query(sql, params, customerMapper).stream().findFirst();
    }

    // Helper to avoid repeating the mapping in Insert and Update
    private MapSqlParameterSource getParameters(Customer item) {
        return new MapSqlParameterSource()
                .addValue("email", item.getEmail())
                .addValue("password", item.getPassword())
                .addValue("firstName", item.getFirstName())
                .addValue("lastName", item.getLastName())
                .addValue("phone", item.getPhone())
                .addValue("address", item.getAddress())
                .addValue("city", item.getCity())
                .addValue("postalCode", item.getPostalCode())
                .addValue("country", item.getCountry())
                .addValue("bank", item.getBank());
    }

    @Override
    public boolean testConnection() {
        try {
            // Simple query to verify DB connectivity
            jdbc.getJdbcTemplate().execute("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
