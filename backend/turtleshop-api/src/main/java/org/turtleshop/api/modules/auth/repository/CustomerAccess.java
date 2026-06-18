package org.turtleshop.api.modules.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.model.CustomerSensitiveData;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerAccess {

    private final NamedParameterJdbcTemplate jdbc;

    /*
     * The repository returns one Customer object even though the database
     * information is stored in two tables.
     *
     * This prevents controllers and the frontend from needing to know about
     * the database split.
     */
    private static final String CUSTOMER_SELECT = """
    SELECT
        c.customer_id,
        c.email,
        c.password,
        c.first_name,
        c.last_name,
        c.created_at,

        csd.phone,
        csd.address,
        csd.city,
        csd.postal_code,
        csd.country,
        csd.updated_at AS sensitive_updated_at

    FROM CUSTOMER c
    LEFT JOIN CUSTOMER_SENSITIVE_DATA csd
        ON csd.customer_id = c.customer_id
    """;

    private final RowMapper<Customer> customerMapper = (rs, rowNum) -> {
        UUID customerId = rs.getObject("customer_id", UUID.class);

        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        Timestamp updatedAtTimestamp = rs.getTimestamp("sensitive_updated_at");

        CustomerSensitiveData sensitiveData =
                CustomerSensitiveData.builder()
                        .customerId(customerId)
                        .phone(rs.getString("phone"))
                        .address(rs.getString("address"))
                        .city(rs.getString("city"))
                        .postalCode(rs.getString("postal_code"))
                        .country(rs.getString("country"))
                        .updatedAt(
                                updatedAtTimestamp == null
                                        ? null
                                        : updatedAtTimestamp.toLocalDateTime()
                        )
                        .build();

        return Customer.builder()
                .customerId(customerId)
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .sensitiveData(sensitiveData)
                .createdAt(
                        createdAtTimestamp == null
                                ? null
                                : createdAtTimestamp.toLocalDateTime()
                )
                .build();
    };

    public Optional<Customer> getById(UUID id) {
        String sql = CUSTOMER_SELECT + """
            WHERE c.customer_id = :id
            """;

        return jdbc.query(
                        sql,
                        new MapSqlParameterSource("id", id),
                        customerMapper
                )
                .stream()
                .findFirst();
    }

    public List<Customer> getAll() {
        String sql = CUSTOMER_SELECT + """
            ORDER BY c.created_at DESC, c.customer_id ASC
            """;

        return jdbc.query(sql, customerMapper);
    }

    public Page<Customer> getPage(Pageable pageable) {
        String sql = CUSTOMER_SELECT + """
            ORDER BY c.created_at DESC, c.customer_id ASC
            LIMIT :limit
            OFFSET :offset
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<Customer> customers =
                jdbc.query(sql, parameters, customerMapper);

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM CUSTOMER",
                new MapSqlParameterSource(),
                Long.class
        );

        return new PageImpl<>(
                customers,
                pageable,
                total == null ? 0 : total
        );
    }

    public Optional<Customer> findByEmail(String email) {
        String sql = CUSTOMER_SELECT + """
            WHERE LOWER(c.email) = LOWER(:email)
            """;

        return jdbc.query(
                        sql,
                        new MapSqlParameterSource("email", email),
                        customerMapper
                )
                .stream()
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        String sql = """
            SELECT COUNT(*)
            FROM CUSTOMER
            WHERE LOWER(email) = LOWER(:email)
            """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("email", email),
                Integer.class
        );

        return count != null && count > 0;
    }

    /*
     * Both database records must be created successfully.
     * If one insert fails, the complete transaction is rolled back.
     */
    @Transactional
    public UUID insertAndReturnId(Customer customer) {
        UUID newCustomerId = UUID.randomUUID();

        CustomerSensitiveData sensitiveData =
                customer.getSensitiveData();

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("customerId", newCustomerId)
                        .addValue("email", customer.getEmail())
                        .addValue("password", customer.getPassword())
                        .addValue("firstName", customer.getFirstName())
                        .addValue("lastName", customer.getLastName())
                        .addValue(
                                "phone",
                                sensitiveData == null
                                        ? null
                                        : sensitiveData.getPhone()
                        )
                        .addValue(
                                "address",
                                sensitiveData == null
                                        ? null
                                        : sensitiveData.getAddress()
                        )
                        .addValue(
                                "city",
                                sensitiveData == null
                                        ? null
                                        : sensitiveData.getCity()
                        )
                        .addValue(
                                "postalCode",
                                sensitiveData == null
                                        ? null
                                        : sensitiveData.getPostalCode()
                        )
                        .addValue(
                                "country",
                                sensitiveData == null
                                        ? null
                                        : sensitiveData.getCountry()
                        );

        String customerSql = """
            INSERT INTO CUSTOMER (
                customer_id,
                email,
                password,
                first_name,
                last_name,
                created_at
            )
            VALUES (
                :customerId,
                :email,
                :password,
                :firstName,
                :lastName,
                NOW()
            )
            """;

        String sensitiveDataSql = """
            INSERT INTO CUSTOMER_SENSITIVE_DATA (
                customer_id,
                phone,
                address,
                city,
                postal_code,
                country,
                updated_at
            )
            VALUES (
                :customerId,
                :phone,
                :address,
                :city,
                :postalCode,
                :country,
                NOW()
            )
            """;

        jdbc.update(customerSql, parameters);
        jdbc.update(sensitiveDataSql, parameters);

        return newCustomerId;
    }

    public void addRoleToCustomer(
            UUID customerId,
            String roleName
    ) {
        String sql = """
            INSERT INTO USER_SYSTEM_ROLES (
                customer_id,
                role_id
            )
            VALUES (
                :customerId,
                (
                    SELECT role_id
                    FROM SYSTEM_ROLES
                    WHERE name = :roleName
                )
            )
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("customerId", customerId)
                        .addValue("roleName", roleName);

        jdbc.update(sql, parameters);
    }

        @Transactional
    public int updateCustomer(UUID customerId, Customer item) {
        String customerSql = """
            UPDATE CUSTOMER
            SET email = :email,
                password = :password,
                first_name = :firstName,
                last_name = :lastName
            WHERE customer_id = :id
            """;

        String sensitiveDataSql = """
            UPDATE CUSTOMER_SENSITIVE_DATA
            SET phone = :phone,
                address = :address,
                city = :city,
                postal_code = :postalCode,
                country = :country,
                updated_at = NOW()
            WHERE customer_id = :id
            """;

        MapSqlParameterSource params = getParameters(item)
                .addValue("id", customerId);

        int updatedCustomer = jdbc.update(customerSql, params);
        int updatedSensitiveData = jdbc.update(sensitiveDataSql, params);

        return updatedCustomer + updatedSensitiveData;
    }

    // HELPER: Converts a Java object into list of values SQL understands
    private MapSqlParameterSource getParameters(Customer item) {
        CustomerSensitiveData sensitiveData = item.getSensitiveData();

        return new MapSqlParameterSource()
                .addValue("email", item.getEmail())
                .addValue("password", item.getPassword())
                .addValue("firstName", item.getFirstName())
                .addValue("lastName", item.getLastName())
                .addValue(
                        "phone",
                        sensitiveData == null
                                ? item.getPhone()
                                : sensitiveData.getPhone()
                )
                .addValue(
                        "address",
                        sensitiveData == null
                                ? item.getAddress()
                                : sensitiveData.getAddress()
                )
                .addValue(
                        "city",
                        sensitiveData == null
                                ? item.getCity()
                                : sensitiveData.getCity()
                )
                .addValue(
                        "postalCode",
                        sensitiveData == null
                                ? item.getPostalCode()
                                : sensitiveData.getPostalCode()
                )
                .addValue(
                        "country",
                        sensitiveData == null
                                ? item.getCountry()
                                : sensitiveData.getCountry()
                );
    }

    // Test Connection
    public boolean testConnection() {
        try {
            jdbc.getJdbcTemplate().execute("SELECT 1");
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}