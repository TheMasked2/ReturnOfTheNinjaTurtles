package org.turtleshop.api.modules.shipment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.shipment.model.Shipment;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentAccess {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Shipment> shipmentRowMapper = (rs, rowNum) -> Shipment.builder()
            .shipmentId(rs.getInt("shipment_id"))
            .orderId(rs.getInt("order_id"))
            .shipmentMethod(rs.getString("shipment_method"))
            .shippingAddress(rs.getString("shipping_address"))
            .build();

    public int createShipment(int orderId, String shipmentMethod, String shippingAddress) {
        String sql = """
                INSERT INTO shipment (order_id, shipment_method, shipping_address)
                VALUES (:orderId, :shipmentMethod, :shippingAddress)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("shipmentMethod", shipmentMethod)
                .addValue("shippingAddress", shippingAddress);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"shipment_id"});
        return keyHolder.getKey().intValue();
    }

    public Optional<Shipment> getShipmentById(int shipmentId) {
        String sql = "SELECT * FROM shipment WHERE shipment_id = :shipmentId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId);
        return jdbc.query(sql, params, shipmentRowMapper).stream().findFirst();
    }

    public Optional<Shipment> getShipmentByOrderId(int orderId) {
        String sql = "SELECT * FROM shipment WHERE order_id = :orderId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        return jdbc.query(sql, params, shipmentRowMapper).stream().findFirst();
    }

    public List<Shipment> getAllShipmentsForAddress(String shippingAddress) {
        String sql = "SELECT * FROM shipment WHERE shipping_address = :shippingAddress";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shippingAddress", shippingAddress);
        return jdbc.query(sql, params, shipmentRowMapper);
    }

    public void updateShipmentMethod(int shipmentId, String shipmentMethod) {
        String sql = "UPDATE shipment SET shipment_method = :shipmentMethod WHERE shipment_id = :shipmentId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shipmentMethod", shipmentMethod)
                .addValue("shipmentId", shipmentId);
        jdbc.update(sql, params);
    }
}
