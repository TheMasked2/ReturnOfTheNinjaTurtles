package org.turtleshop.api.modules.wishlist.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import org.turtleshop.api.modules.wishlist.model.WishlistItem;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WishlistItemRepositoryImpl implements WishlistItemRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<WishlistItem> wishlistItemMapper = (rs, rowNum) -> WishlistItem.builder()
            .wishlistItemId(rs.getInt("wishlist_item_id"))
            .wishlistId(rs.getInt("wishlist_id"))
            .productId(rs.getInt("product_id"))
            .addedAt(rs.getTimestamp("added_at") != null ? rs.getTimestamp("added_at").toLocalDateTime() : null)
            .build();

    // --- GET ---

    @Override
    public List<WishlistItem> getAll() {
        String sql = "SELECT * FROM WISHLIST_ITEM";
        return jdbc.query(sql, wishlistItemMapper);
    }

    @Override
    public List<WishlistItem> getAllByWishlistId(Integer wishlistId) {
        String sql = "SELECT * FROM WISHLIST_ITEM WHERE wishlist_id = :wishlistId";
        return jdbc.query(sql, new MapSqlParameterSource("wishlistId", wishlistId), wishlistItemMapper);
    }

    @Override
    public Optional<WishlistItem> getByWishlistItemId(Integer wishlistItemId) {
        if (wishlistItemId == null) return Optional.empty();
        
        String sql = "SELECT * FROM WISHLIST_ITEM WHERE wishlist_item_id = :wishlistItemId";
        return jdbc.query(sql, new MapSqlParameterSource("wishlistItemId", wishlistItemId), wishlistItemMapper)
                .stream().findFirst();
    }

    @Override
    public Optional<WishlistItem> getByWishlistId(Integer wishlistId) {
        if (wishlistId == null) return Optional.empty();
        
        // Note: As you defined it in the interface, this returns ONE item for a given wishlist.
        // Typically a wishlist has many items, so getAllByWishlistId is usually preferred.
        String sql = "SELECT * FROM WISHLIST_ITEM WHERE wishlist_id = :wishlistId";
        return jdbc.query(sql, new MapSqlParameterSource("wishlistId", wishlistId), wishlistItemMapper)
                .stream().findFirst();
    }

    @Override
    public Optional<WishlistItem> getByProductId(Integer productId) {
        if (productId == null) return Optional.empty();
        
        String sql = "SELECT * FROM WISHLIST_ITEM WHERE product_id = :productId";
        return jdbc.query(sql, new MapSqlParameterSource("productId", productId), wishlistItemMapper)
                .stream().findFirst();
    }

    @Override
    public boolean existsByWishlistIdAndProductId(Integer wishlistId, Integer productId) {
        if (wishlistId == null || productId == null) return false;

        String sql = "SELECT COUNT(*) FROM WISHLIST_ITEM WHERE wishlist_id = :wishlistId AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("wishlistId", wishlistId)
                .addValue("productId", productId);
        
        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    // --- INSERT ---

    @Override
    public void insert(Integer wishlistId, Integer productId) {
        if (wishlistId == null || productId == null) {
            throw new IllegalArgumentException("wishlistId and productId cannot be null");
        }

        String sql = "INSERT INTO WISHLIST_ITEM (wishlist_id, product_id) VALUES (:wishlistId, :productId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("wishlistId", wishlistId)
                .addValue("productId", productId);
        
        jdbc.update(sql, params);
    }

    @Override
    public Integer insertAndReturnId(Integer wishlistId, Integer productId) {
        if (wishlistId == null || productId == null) {
            throw new IllegalArgumentException("wishlistId and productId cannot be null");
        }

        String sql = "INSERT INTO WISHLIST_ITEM (wishlist_id, product_id) VALUES (:wishlistId, :productId) RETURNING wishlist_item_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("wishlistId", wishlistId)
                .addValue("productId", productId);
        
        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // --- UPDATE ---

    @Override
    public void updateProductId(Integer wishlistItemId, Integer newProductId) {
        if (wishlistItemId == null || newProductId == null) {
            throw new IllegalArgumentException("wishlistItemId and newProductId cannot be null");
        }

        String sql = "UPDATE WISHLIST_ITEM SET product_id = :newProductId WHERE wishlist_item_id = :wishlistItemId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("newProductId", newProductId)
                .addValue("wishlistItemId", wishlistItemId);
        
        jdbc.update(sql, params);
    }

    // --- DELETE ---

    @Override
    public void deleteById(Integer wishlistItemId) {
        if (wishlistItemId == null) return;

        String sql = "DELETE FROM WISHLIST_ITEM WHERE wishlist_item_id = :wishlistItemId";
        jdbc.update(sql, new MapSqlParameterSource("wishlistItemId", wishlistItemId));
    }

    @Override
    public void deleteByWishlistId(Integer wishlistId) {
        if (wishlistId == null) return;

        String sql = "DELETE FROM WISHLIST_ITEM WHERE wishlist_id = :wishlistId";
        jdbc.update(sql, new MapSqlParameterSource("wishlistId", wishlistId));
    }

    @Override
    public void deleteItemFromWishlist(Integer wishlistId, Integer productId) {
        if (wishlistId == null || productId == null) return;

        String sql = "DELETE FROM WISHLIST_ITEM WHERE wishlist_id = :wishlistId AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("wishlistId", wishlistId)
                .addValue("productId", productId);
        
        jdbc.update(sql, params);
    }
}