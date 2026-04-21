package org.turtleshop.api.modules.auth.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.core.repository.GenericAccess;
import org.turtleshop.api.modules.auth.model.Customer;

@Repository
public class CustomerAccess extends GenericAccess<Customer, Integer> {

    public CustomerAccess(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override protected String getTable() { return "CUSTOMER"; }
    @Override protected String getPrimaryKey() { return "costumer_id"; }

    /**
     * The Manual Mapper using the Lombok Builder
     */
    @Override
    protected RowMapper<Customer> getRowMapper() {
        return (rs, rowNum) -> Customer.builder()
                .customerId(rs.getInt("costumer_id"))
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
    }

    @Override
    public void insertAsync(Customer item) {
        String sql = """
            INSERT INTO CUSTOMER (email, password, first_name, last_name, phone, address, city, postal_code, country, bank, created_at)
            VALUES (:email, :password, :firstName, :lastName, :phone, :address, :city, :postalCode, :country, :bank, NOW())
        """;

        // Using MapSqlParameterSource to manually map fields for the query
        MapSqlParameterSource params = new MapSqlParameterSource()
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

        jdbc.update(sql, params);
    }

    @Override
    public void updateAsync(Customer item) {
        String sql = """
            UPDATE CUSTOMER 
            SET email = :email, first_name = :firstName, last_name = :lastName 
            WHERE costumer_id = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", item.getEmail())
                .addValue("firstName", item.getFirstName())
                .addValue("lastName", item.getLastName())
                .addValue("id", item.getCustomerId());

        jdbc.update(sql, params);
    }
}