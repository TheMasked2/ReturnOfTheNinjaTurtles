package org.turtleshop.api.modules.order.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.model.Order;
import org.turtleshop.api.modules.order.model.OrderSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderAccess {
    private final NamedParameterJdbcTemplate jdbc;

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> Order.builder()
            .orderId(rs.getInt("order_id"))
            .customerId(rs.getObject("customer_id",java.util.UUID.class))
            .orderDate(rs.getTimestamp("order_date").toLocalDateTime())
            .status(OrderStatus.valueOf(rs.getString("status").toUpperCase()))
            .totalAmount(rs.getBigDecimal("total_amount"))
            .build();

    // MAPPER: Maps row from db result into a Java Object
    private final RowMapper<OrderSummary> orderSummaryRowMapper = (rs, rowNum) -> OrderSummary.builder()
            .orderId(rs.getInt("order_id"))
            .customerId(rs.getObject("customer_id", java.util.UUID.class))
            .customerEmail(rs.getString("customer_email"))
            .orderDate(rs.getTimestamp("order_date").toLocalDateTime())
            .status(OrderStatus.valueOf(rs.getString("status").toUpperCase()))
            .totalAmount(rs.getBigDecimal("total_amount"))
            .itemLines(rs.getLong("item_lines"))
            .totalItems(rs.getLong("total_items"))
            .build();

    public int createOrder(UUID customerId, OrderStatus status, BigDecimal totalAmount) {
        String sql = """
            INSERT INTO orders (customer_id, order_date, status, total_amount)
            VALUES (:customerId, CURRENT_TIMESTAMP, :status, :totalAmount)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("status", status.name())
                .addValue("totalAmount", totalAmount);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"order_id"});

        return keyHolder.getKey().intValue();
    }

    // Get Order by ID
    public Optional<Order> getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        return jdbc.query(sql, params, orderRowMapper).stream().findFirst();
    }

    // Get all Orders by CustomerID
    public List<Order> getAllOrdersById(UUID customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId);
        return jdbc.query(sql, params, orderRowMapper);
    }

    public List<OrderSummary> getOrderSummaries(int limit) {
        String sql = " SELECT * FROM v_order_summary ORDER BY order_date DESC LIMIT :limit ";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit);
        return jdbc.query(sql, params, orderSummaryRowMapper);
    }

    // Update Order status
    public void updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = :status WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("status", status.name());
        jdbc.update(sql, params);
    }

    // Update total amount in Order
    public void updateTotalAmount(int orderId, BigDecimal totalAmount) {
        String sql = "UPDATE orders SET total_amount = :totalAmount WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("totalAmount", totalAmount);
        jdbc.update(sql, params);
    }

    // Delete Order based on the OrderID
    public void deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        jdbc.update(sql, params);
    }
}
