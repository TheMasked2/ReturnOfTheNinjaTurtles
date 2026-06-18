package org.turtleshop.api.modules.shipment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;
import org.turtleshop.api.modules.shipment.model.ShipmentStatusLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentStatusLogAccess {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<ShipmentStatusLog> shipmentStatusLogRowMapper = (rs, rowNum) -> ShipmentStatusLog.builder()
            .logId(rs.getInt("log_id"))
            .shipmentId(rs.getInt("shipment_id"))
            .status(ShipmentStatus.valueOf(rs.getString("status")))
            .statusChangeDate(rs.getTimestamp("status_change_date").toLocalDateTime())
            .notes(rs.getString("notes"))
            .build();

    public int createShipmentStatusLog(int shipmentId, ShipmentStatus status) {
        String sql = """
                INSERT INTO shipment_status_log (shipment_id, status)
                VALUES (:shipmentId, :status)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId)
                .addValue("status", status.name());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"log_id"});
        return keyHolder.getKey().intValue();
    }

    public Optional<ShipmentStatusLog> getShipmentStatusLog(int logId) {
        String sql = "SELECT * FROM shipment_status_log WHERE log_id = :logId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("logId", logId);
        return jdbc.query(sql, params, shipmentStatusLogRowMapper).stream().findFirst();
    }

    public List<ShipmentStatusLog> getAllShipmentStatusLogsOfShipment(int shipmentId) {
        String sql = "SELECT * FROM shipment_status_log WHERE shipment_id = :shipmentId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId);
        return jdbc.query(sql, params, shipmentStatusLogRowMapper);
    }

    public Optional<ShipmentStatusLog> getLatestShipmentStatusLog(int shipmentId) {
        String sql = """
                SELECT *
                FROM shipment_status_log
                WHERE shipment_id = :shipmentId
                ORDER BY status_change_date DESC
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("shipmentId", shipmentId);
        return jdbc.query(sql, params, shipmentStatusLogRowMapper).stream().findFirst();
    }

    public void updateNotes(int logId, String notes) {
        String sql = "UPDATE shipment_status_log SET notes = :notes WHERE log_id = :logId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("notes", notes)
                .addValue("logId", logId);
        jdbc.update(sql, params);
    }


}
