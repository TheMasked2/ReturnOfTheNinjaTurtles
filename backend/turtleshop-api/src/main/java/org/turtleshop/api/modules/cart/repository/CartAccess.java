package org.turtleshop.api.modules.cart.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.cart.model.Cart;
import java.util.UUID;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class CartAccess {

    private final NamedParameterJdbcTemplate jdbc;

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<Cart> cartMapper = (rs, rowNum) -> Cart.builder()
            .cartId(rs.getInt("cart_id"))
            .customerId(rs.getObject("customer_id", java.util.UUID.class))
            .orderId(rs.getObject("order_id", Integer.class))
            .status(CartStatus.valueOf(rs.getString("status").toUpperCase()))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    // Insert Cart
    public void insertCart(UUID customerId) {
        String sql = """
                INSERT INTO cart (customer_id, status, created_at)
                VALUES (:customerId, :status, :createdAt)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customer_id", customerId)
                .addValue("status", CartStatus.ACTIVE.name())
                .addValue("createdAt", LocalDateTime.now());

        jdbc.update(sql, params);
    }

    // Get Cart by ID
    public Optional<Cart> getCartById(int cartId) {
        String sql = "SELECT * FROM cart WHERE cart_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", cartId), cartMapper).steam().findFirst();
    }

    // Get Active Cart by Customer ID
    public Optional<Cart> getActiveCartByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM cart WHERE customer_id = :id AND status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", customerId)
                .addValue("status", CartStatus.ACTIVE.name().toLowerCase());
        return jdbc.query(sql, params, cartMapper).stream().findFirst();
    }

    // Get All Carts that are Active
    public Optional<Cart> getAllActiveCarts() {
        String sql = "SELECT * FROM cart WHERE status = :status";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .newValue("status", CartStatus.Active.name());
        return jdbc.query(sql, params, cartMapper);
    }

    // Update Cart Status by ID
    public void updateCartStatus(int cartId, CartStatus status) {
        String sql = "UPDATE cart SET status = :status WHERE cart_id = :cartId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .newValue("status", status.name())
                .newValue("cartId", cartId);
        jdbc.update(sql, params);

    }

    // Delete Cart
    public void deleteCart(int cartId) {
        String sql = "DELETE FROM cart WHERE cart_id = :cartId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .newValue("cartId", cartId);
        jdbc.update(sql, params);
    }

}