package org.turtleshop.api.modules.inventory.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.inventory.model.InventoryModel;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryAccess {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<InventoryModel> inventoryMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("version");
        return InventoryModel.builder()
                .inventoryId(rs.getInt("inventory_id"))
                .productId(rs.getInt("product_id"))
                .quantityAvailable(rs.getInt("quantity_available"))
                .quantityReserved(rs.getInt("quantity_reserved"))
                .version(ts != null ? ts.toLocalDateTime() : null)
                .build();
    };

    public Optional<InventoryModel> findById(int inventoryId) {
        String sql = "SELECT * FROM INVENTORY WHERE inventory_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", inventoryId), inventoryMapper)
                .stream().findFirst();
    }

    public List<InventoryModel> findByQuantityAvailableGreaterOrEqual(int quantity) {
        String sql = "SELECT * FROM INVENTORY WHERE quantity_available >= :quantity ORDER BY quantity_available";
        return jdbc.query(sql, new MapSqlParameterSource("quantity", quantity), inventoryMapper);
    }

    public void updateInventoryByProductId(int productId, int quantityAvailable, int quantityReserved) {
        String sql = "UPDATE INVENTORY SET quantity_available = :quantityAvailable, quantity_reserved = :quantityReserved WHERE product_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("quantityAvailable", quantityAvailable)
                .addValue("quantityReserved", quantityReserved)
                .addValue("id", productId);
        jdbc.update(sql, params);
    }

    public Optional<InventoryModel> findByProductId(int productId) {
        String sql = "SELECT * FROM INVENTORY WHERE product_id = :productId";
        return jdbc.query(sql, new MapSqlParameterSource("productId", productId), inventoryMapper)
                .stream().findFirst();
    }

    public int insertInventory(InventoryModel inventory) {
        String sql = "INSERT INTO INVENTORY (product_id, quantity_available, quantity_reserved) " +
                "VALUES (:productId, :quantityAvailable, :quantityReserved)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", inventory.getProductId())
                .addValue("quantityAvailable", inventory.getQuantityAvailable())
                .addValue("quantityReserved", inventory.getQuantityReserved());
        jdbc.update(sql, params);
        return jdbc.getJdbcTemplate().queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
    }

    public List<InventoryModel> findLastUpdated(int limit) {
        String sql = "SELECT * FROM INVENTORY ORDER BY version DESC LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("limit", limit), inventoryMapper);
    }

    public void deleteByProductId(int productId) {
        String sql = "DELETE FROM INVENTORY WHERE product_id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", productId));
    }

    public List<InventoryModel> findAll(int offset, int limit) {
        String sql = "SELECT * FROM INVENTORY ORDER BY inventory_id LIMIT :limit OFFSET :offset";
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset), inventoryMapper);
    }

    public List<InventoryModel> findByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM INVENTORY WHERE product_id IN (:ids)";
        return jdbc.query(sql, new MapSqlParameterSource("ids", productIds), inventoryMapper);
    }

    public List<InventoryModel> findLowStock(int threshold) {
        String sql = "SELECT * FROM INVENTORY WHERE quantity_available <= :threshold ORDER BY quantity_available";
        return jdbc.query(sql, new MapSqlParameterSource("threshold", threshold), inventoryMapper);
    }

    public void updateQuantitiesByProductId(int productId, int quantityAvailable, int quantityReserved) {
        String sql = "UPDATE INVENTORY SET quantity_available = :available, quantity_reserved = :reserved WHERE product_id = :productId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("available", quantityAvailable)
                .addValue("reserved", quantityReserved)
                .addValue("productId", productId));
    }
}