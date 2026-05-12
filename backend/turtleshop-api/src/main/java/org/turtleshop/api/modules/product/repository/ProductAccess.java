package org.turtleshop.api.modules.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.product.model.ProductModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductAccess {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<ProductModel> productMapper = (rs, rowNum) -> {
        ProductModel product = new ProductModel();
        product.setProductId(rs.getInt("product_id"));
        product.setBasePrice(rs.getBigDecimal("base_price"));
        return product;
    };

    public List<ProductModel> findAll() {
        String sql = "SELECT product_id, base_price FROM PRODUCT";
        return jdbc.query(sql, productMapper);
    }

    public Optional<ProductModel> findById(int id) {
        String sql = "SELECT product_id, base_price FROM PRODUCT WHERE product_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", id), productMapper)
                .stream()
                .findFirst();
    }

    public int insert(ProductModel product) {
        int nextId = jdbc.queryForObject("SELECT COALESCE(MAX(product_id), 0) + 1 FROM PRODUCT",
                new MapSqlParameterSource(), Integer.class);
        product.setProductId(nextId);

        String sql = """
            INSERT INTO PRODUCT (product_id, base_price)
            VALUES (:id, :price)
        """;

        jdbc.update(sql, getParameters(product));
        return nextId;
    }

    public void update(ProductModel product) {
        String sql = """
            UPDATE PRODUCT
            SET base_price = :price
            WHERE product_id = :id
        """;

        jdbc.update(sql, getParameters(product));
    }

    public void deleteById(int id) {
        jdbc.update("DELETE FROM PRODUCT WHERE product_id = :id",
                new MapSqlParameterSource("id", id));
    }

    private MapSqlParameterSource getParameters(ProductModel product) {
        return new MapSqlParameterSource()
                .addValue("id", product.getProductId())
                .addValue("price", product.getBasePrice());
    }
}