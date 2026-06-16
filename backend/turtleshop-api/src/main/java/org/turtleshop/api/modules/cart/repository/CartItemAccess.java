package org.turtleshop.api.modules.cart.repository;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.turtleshop.api.modules.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartItemAccess {

    private final NamedParameterJdbcTemplate jdbc;

    //Mapper: Maps the rows from the database result into a Java Object
    private final RowMapper<CartItem> cartItemMapper = (rs, rowNum) -> CartItem.builder()
            .cartItemId(rs.getInt("cart_item_id"))
            .cartId(rs.getInt("cart_id"))
            .productId(rs.getInt("product_id"))
            .quantity(rs.getInt("quantity"))
            .build();

    // Inserts Cart Item
    public int insertCartItem(int cartId, int productId, int quantity) {
        String sql = """
                INSERT INTO cart_item (cart_id, product_id, quantity)
                VALUES (:cartId, :productId, :quantity)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartId)
                .addValue("productId", productId)
                .addValue("quantity", quantity);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"cart_item_id"});
        return keyHolder.getKey().intValue();
    }

    // Get CartItem by CartItemId in a Cart
    public Optional<CartItem> getCartItemById(int cartItemId) {
        String sql = "SELECT * FROM cart_item WHERE cart_item_id = :cartItemId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId);
        return jdbc.query(sql, params, cartItemMapper).stream().findFirst();
    }

    // Get all CartItems in a Cart
    public List<CartItem> getAllCartItems(int cartId) {
        String sql = "SELECT * FROM cart_item WHERE cart_id = :cartId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartId);
        return jdbc.query(sql, params, cartItemMapper);
    }

    // Update quantity of a CartItem
    public void updateCartItemQuantity(int cartItemId, int quantity) {
        String sql = """
            UPDATE cart_item
            SET quantity = :quantity
            WHERE cart_item_id = :cartItemId
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId)
                .addValue("quantity", quantity);
        jdbc.update(sql, params);
    }

    // Delete Cart by CartItemID
    public void deleteCartItem(int cartItemId) {
        String sql = "DELETE FROM cart_item WHERE cart_item_id = :cartItemId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartItemId", cartItemId);
        jdbc.update(sql, params);
    }

    public Optional<CartItem> getCartItemByCartIdAndProductId(int cartId, int productId) {
        String sql = "SELECT * FROM cart_item WHERE cart_id = :cartId AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("cartId", cartId)
                .addValue("productId", productId);
        return jdbc.query(sql, params, cartItemMapper).stream().findFirst();
    }
}