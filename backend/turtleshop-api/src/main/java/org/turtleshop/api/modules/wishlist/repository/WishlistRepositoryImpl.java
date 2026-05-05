package org.turtleshop.api.modules.wishlist.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.wishlist.model.Wishlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WishlistRepositoryImpl implements WishlistRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Wishlist> wishlistMapper = ( rs, rowNum) -> WishList.builder()
            .wishlistId(rs.getInt("wishlist_id"))
            .customerId(rs.getObject("customer_id", java.util.UUID.class))
            .build();

    // Get
    public List<Wishlist> getAll() {
        String sql = "SELECT * FROM WISHLIST";
        List<Wishlist> result = jdbc.query(sql, wishlistMapper);
        return result.isEmpty() ? null : result;
    }

    public Optional<Wishlist> getByWishlistId(Integer id) {
        String sql = "SELECT * FROM WISHLIST WHERE wishlist_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", id), wishlistMapper)
                .stream().findFirst();
    }

    public Optional<Wishlist> getByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM WISHLIST WHERE customer_id = :customerId";
        return jdbc.query(sql, new MapSqlParameterSource("customerId", customerId), wishlistMapper)
                .stream().findFirst();
    }
    
    // Insert
    public void insert(UUID customerId) {
        String sql = "INSERT INTO WISHLIST (customer_id) VALUES (:customerId)";
        jdbc.update(sql, new MapSqlParameterSource("customerId", customerId));
    }

    public Integer insertAndReturnId(UUID customerId) {
        String sql = "INSERT INTO WISHLIST (customer_id) VALUES (:customerId) RETURNING wishlist_id";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("customerId", customerId), Integer.class);
    }

    // Update
    public void updateCustomerId(Integer wishlistId, UUID newCustomerId) {
        String sql = "UPDATE WISHLIST SET customer_id = :newCustomerId WHERE wishlist_id = :wishlistId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("newCustomerId", newCustomerId)
                .addValue("wishlistId", wishlistId));
    }

    // Delete
    public void deleteById(Integer wishlistId) {
        String sql = "DELETE FROM WISHLIST WHERE wishlist_id = :wishlistId";
        jdbc.update(sql, new MapSqlParameterSource("wishlistId", wishlistId));
    }

    public void deleteByCustomerId(UUID customerId) {
        String sql = "DELETE FROM WISHLIST WHERE customer_id = :customerId";
        jdbc.update(sql, new MapSqlParameterSource("customerId", customerId));
    }
}
