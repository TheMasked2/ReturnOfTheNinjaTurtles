package org.turtleshop.api.modules.productcategory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.productcategory.model.ProductCategoryModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
// Data access for product-category mappings
public class ProductCategoryAccess {
    private final NamedParameterJdbcTemplate jdbc;

    // Map JDBC rows to product-category model
    private final RowMapper<ProductCategoryModel> rowMapper = (rs, rowNum) -> {
        ProductCategoryModel mapping = new ProductCategoryModel();
        mapping.setProductId(rs.getInt("product_id"));
        mapping.setCategoryId(rs.getInt("category_id"));
        return mapping;
    };

    public List<ProductCategoryModel> findAll() {
        String sql = "SELECT product_id, category_id FROM PRODUCT_CATEGORY";
        return jdbc.query(sql, rowMapper);
    }

    // Find mapping by composite primary key
    public Optional<ProductCategoryModel> findById(int productId, int categoryId) {
        String sql = "SELECT product_id, category_id FROM PRODUCT_CATEGORY WHERE product_id = :productId AND category_id = :categoryId";
        return jdbc.query(sql, new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("categoryId", categoryId),
                rowMapper)
                .stream()
                .findFirst();
    }

    // Insert a new product-category mapping
    public void insert(ProductCategoryModel mapping) {
        String sql = """
            INSERT INTO PRODUCT_CATEGORY (product_id, category_id)
            VALUES (:productId, :categoryId)
        """;

        jdbc.update(sql, getParameters(mapping));
    }

    // Update an existing mapping by composite key
    public void update(int oldProductId, int oldCategoryId, ProductCategoryModel mapping) {
        String sql = """
            UPDATE PRODUCT_CATEGORY
            SET product_id = :newProductId,
                category_id = :newCategoryId
            WHERE product_id = :oldProductId
              AND category_id = :oldCategoryId
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("newProductId", mapping.getProductId())
                .addValue("newCategoryId", mapping.getCategoryId())
                .addValue("oldProductId", oldProductId)
                .addValue("oldCategoryId", oldCategoryId));
    }

    // Delete mapping by composite key
    public void deleteById(int productId, int categoryId) {
        jdbc.update("DELETE FROM PRODUCT_CATEGORY WHERE product_id = :productId AND category_id = :categoryId",
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("categoryId", categoryId));
    }

    // Build query parameters from the model
    private MapSqlParameterSource getParameters(ProductCategoryModel mapping) {
        return new MapSqlParameterSource()
                .addValue("productId", mapping.getProductId())
                .addValue("categoryId", mapping.getCategoryId());
    }
}
