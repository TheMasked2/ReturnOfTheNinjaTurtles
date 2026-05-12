package org.turtleshop.api.modules.transaction.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.transaction.model.PaymentMethodModel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentMethodAccess {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<PaymentMethodModel> paymentMethodMapper = (rs, rowNum) ->
            PaymentMethodModel.builder()
                    .paymentMethodId(rs.getInt("payment_method_id"))
                    .provider(rs.getString("provider"))
                    .type(rs.getString("type"))
                    .build();

    public List<PaymentMethodModel> findAll() {
        String sql = "SELECT * FROM PAYMENT_METHOD ORDER BY payment_method_id";
        return jdbc.query(sql, paymentMethodMapper);
    }

    public Optional<PaymentMethodModel> findById(int paymentMethodId) {
        String sql = "SELECT * FROM PAYMENT_METHOD WHERE payment_method_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", paymentMethodId), paymentMethodMapper)
                .stream().findFirst();
    }

    public int insert(PaymentMethodModel paymentMethod) {
        String sql = "INSERT INTO PAYMENT_METHOD (provider, type) VALUES (:provider, :type)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("provider", paymentMethod.getProvider())
                .addValue("type", paymentMethod.getType());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"payment_method_id"});
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }
}