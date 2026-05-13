package org.turtleshop.api.modules.order.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.order.model.OrderItem;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderItemAccess {
    private final NamedParameterJdbcTemplate jdbc;

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> OrderItem.builder()
            .orderItemId(rs.getInt("order_item_id"))
            .orderId(rs.getInt("order_id"))
            .productId(rs.getInt("product_id"))
            .quantity(rs.getInt("quantity"))
            .build();

    // Add OrderItem to the Order
    public int addOrderItemToOrder(int orderId, int productId, int quantity) {
        String sql = """
                INSERT INTO order_item (order_id, product_id, quantity)
                VALUES (:orderId, :productId, :quantity)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("productId", productId)
                .addValue("quantity", quantity);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"order_item_id"});
        return keyHolder.getKey().intValue();
    }

    // Get OrderItem from Order
    public Optional<OrderItem> getOrderItemById(int orderItemId) {
        String sql = "SELECT * FROM order_item WHERE order_item_id = :orderItemId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderItemId", orderItemId);
        return jdbc.query(sql, params, orderItemRowMapper).stream().findFirst();
    }

    // Get all OrderItems from Order
    public List<OrderItem> getAllOrderItems(int orderId) {
        String sql = "SELECT * FROM order_item WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        return jdbc.query(sql, params, orderItemRowMapper);
    }

    // Delete OrderItem from Order
    public void deleteOrderItem(int orderItemId) {
        String sql = "DELETE FROM order_item WHERE order_item_id = :orderItemId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderItemId", orderItemId);
        jdbc.update(sql, params);
    }
}
