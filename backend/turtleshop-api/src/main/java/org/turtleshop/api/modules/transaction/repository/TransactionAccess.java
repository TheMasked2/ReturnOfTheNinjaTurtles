package org.turtleshop.api.modules.transaction.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.transaction.model.TransactionModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionAccess {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<TransactionModel> transactionMapper = (rs, rowNum) ->
            TransactionModel.builder()
                    .transactionId(rs.getInt("transaction_id"))
                    .orderId(rs.getInt("order_id"))
                    .paymentMethodId(rs.getInt("payment_method_id"))
                    .amount(rs.getBigDecimal("amount"))
                    .status(rs.getString("status"))
                    .transactionDate(rs.getTimestamp("transaction_date").toLocalDateTime())
                    .build();

    public List<TransactionModel> findAll(int offset, int limit) {
        String sql = "SELECT * FROM TRANSACTION ORDER BY transaction_id LIMIT :limit OFFSET :offset";
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset), transactionMapper);
    }

    public Optional<TransactionModel> findById(int transactionId) {
        String sql = "SELECT * FROM TRANSACTION WHERE transaction_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", transactionId), transactionMapper)
                .stream().findFirst();
    }

    public List<TransactionModel> findByOrderId(int orderId) {
        String sql = "SELECT * FROM TRANSACTION WHERE order_id = :orderId ORDER BY transaction_date DESC";
        return jdbc.query(sql, new MapSqlParameterSource("orderId", orderId), transactionMapper);
    }

    public List<TransactionModel> findByPaymentMethodId(int paymentMethodId) {
        String sql = "SELECT * FROM TRANSACTION WHERE payment_method_id = :paymentMethodId ORDER BY transaction_date DESC";
        return jdbc.query(sql, new MapSqlParameterSource("paymentMethodId", paymentMethodId), transactionMapper);
    }

    public List<TransactionModel> findByStatus(String status) {
        String sql = "SELECT * FROM TRANSACTION WHERE status = :status ORDER BY transaction_date DESC";
        return jdbc.query(sql, new MapSqlParameterSource("status", status), transactionMapper);
    }

    public int insert(TransactionModel transaction) {
        String sql = "INSERT INTO TRANSACTION (order_id, payment_method_id, amount, status) " +
                "VALUES (:orderId, :paymentMethodId, :amount, :status)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", transaction.getOrderId())
                .addValue("paymentMethodId", transaction.getPaymentMethodId())
                .addValue("amount", transaction.getAmount())
                .addValue("status", transaction.getStatus());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"transaction_id"});
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public void update(int transactionId, BigDecimal amount, String status, Integer paymentMethodId) {
        String sql = "UPDATE TRANSACTION SET amount = :amount, status = :status, payment_method_id = :paymentMethodId " +
                "WHERE transaction_id = :id";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("amount", amount)
                .addValue("status", status)
                .addValue("paymentMethodId", paymentMethodId)
                .addValue("id", transactionId));
    }

    public void deleteById(int transactionId) {
        String sql = "DELETE FROM TRANSACTION WHERE transaction_id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", transactionId));
    }
}