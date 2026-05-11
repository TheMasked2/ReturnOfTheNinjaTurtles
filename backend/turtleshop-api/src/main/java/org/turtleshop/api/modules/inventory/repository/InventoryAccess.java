package org.turtleshop.api.modules.inventory.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.inventory.model.InventoryModel;

@Repository
@RequiredArgsConstructor
public class InventoryAccess {
    
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<InventoryModel> inventoryMapper = (rs, rowNum) -> InventoryModel.builder()
            .inventoryId(rs.getInt("inventory_id"))
            .productId(rs.getInt("product_id"))
            .quantityAvailable(rs.getInt("quantity_available"))
            .quantityReserved(rs.getInt("quantity_reserved"))
            .version(rs.getTimestamp("version").toLocalDateTime())
            .build();

    // find by inventory ID
    public java.util.Optional<InventoryModel> findById(int inventoryId) {
        String sql = "SELECT * FROM INVENTORY WHERE inventory_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", inventoryId), inventoryMapper)
                .stream().findFirst();
    }

    // Find by quantity available
    public java.util.Optional<InventoryModel> findByQuantityAvailable(int quantity) {
        String sql = "SELECT * FROM INVENTORY WHERE quantity_available >= :quantity";
        return jdbc.query(sql, new MapSqlParameterSource("quantity", quantity), inventoryMapper)
                .stream().findFirst();
    }

     // Update inventory quantities
     public void updateInventoryByProductId(int productId, int quantityAvailable, int quantityReserved) {
        String sql = "UPDATE INVENTORY SET quantity_available = :quantityAvailable, quantity_reserved = :quantityReserved, version = CURRENT_TIMESTAMP WHERE product_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("quantityAvailable", quantityAvailable)
                .addValue("quantityReserved", quantityReserved)
                .addValue("id", productId);
        jdbc.update(sql, params);
    }

    // Find by product ID
    public java.util.Optional<InventoryModel> findByProductId(int productId) {
        String sql = "SELECT * FROM INVENTORY WHERE product_id = :productId";
        return jdbc.query(sql, new MapSqlParameterSource("productId", productId), inventoryMapper)
                .stream().findFirst();
    }

    // Insert new inventory record
    public int insertInventory(InventoryModel inventory) {
        String sql = "INSERT INTO INVENTORY (product_id, quantity_available, quantity_reserved, version) VALUES (:productId, :quantityAvailable, :quantityReserved, CURRENT_TIMESTAMP) RETURNING inventory_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", inventory.getProductId())
                .addValue("quantityAvailable", inventory.getQuantityAvailable())
                .addValue("quantityReserved", inventory.getQuantityReserved());
        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // Find last updated inventory records
    public java.util.List<InventoryModel> findLastUpdated(int limit) {
        String sql = "SELECT * FROM INVENTORY ORDER BY version DESC LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("limit", limit), inventoryMapper);
    }

    // Delete inventory record by Product ID
    public void deleteByProductId(int productId) {
        String sql = "DELETE FROM INVENTORY WHERE product_id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", productId));
    }
}
