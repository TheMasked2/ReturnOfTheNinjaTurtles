package org.turtleshop.api.core.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import java.util.List;
import java.util.Optional;

public abstract class GenericAccess<T, K> implements IGenericAccess<T, K> {

    protected final NamedParameterJdbcTemplate jdbc;

    // These force the specific implementation to provide the details
    protected abstract String getTable();
    protected abstract String getPrimaryKey();
    protected abstract RowMapper<T> getRowMapper();

    protected GenericAccess(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<T> getById(K id) {
        String query = String.format("SELECT * FROM %s WHERE %s = :id", getTable(), getPrimaryKey());
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        // Use the mapper provided by the child class
        return jdbc.query(query, params, getRowMapper())
                .stream()
                .findFirst();
    }

    @Override
    public List<T> getAll() {
        String query = "SELECT * FROM " + getTable();
        return jdbc.query(query, getRowMapper());
    }

    @Override
    public void delete(K id) {
        String query = String.format("DELETE FROM %s WHERE %s = :id", getTable(), getPrimaryKey());
        jdbc.update(query, new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean testConnection() {
        try {
            // Standard JDBC way to test connection
            Integer result = jdbc.getJdbcTemplate().queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            return false;
        }
    }
}