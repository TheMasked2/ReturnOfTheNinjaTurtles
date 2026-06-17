package org.turtleshop.api.modules.product.repository;

import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

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

    public List<ProductModel> findAllByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = """
            SELECT product_id, base_price
            FROM PRODUCT
            WHERE product_id IN (:ids)
            ORDER BY product_id
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource("ids", ids),
                productMapper);
    }

    public List<ProductModel> findPage(int limit, int offset) {
        String sql = """
            SELECT product_id, base_price
            FROM PRODUCT
            ORDER BY product_id
            LIMIT :limit OFFSET :offset
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("limit", limit)
                        .addValue("offset", offset),
                productMapper);
    }

    public int countAll() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM PRODUCT",
                new MapSqlParameterSource(), Integer.class);
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

    public int countFiltered(
            List<Integer> searchIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer categoryId) {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT p.product_id)
            FROM PRODUCT p
            """);

        if (categoryId != null) {
            sql.append(" JOIN PRODUCT_CATEGORY pc ON pc.product_id = p.product_id");
        }

        sql.append(" WHERE 1=1");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (searchIds != null) {
            sql.append(" AND p.product_id IN (:searchIds)");
            params.addValue("searchIds", searchIds);
        }
        if (minPrice != null) {
            sql.append(" AND p.base_price >= :minPrice");
            params.addValue("minPrice", minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.base_price <= :maxPrice");
            params.addValue("maxPrice", maxPrice);
        }
        if (categoryId != null) {
            sql.append(" AND pc.category_id = :categoryId");
            params.addValue("categoryId", categoryId);
        }

        return jdbc.queryForObject(sql.toString(), params, Integer.class);
    }

    public List<ProductModel> findPageWithFilters(
            List<Integer> searchIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer categoryId,
            String sortBy,
            int limit,
            int offset) {

        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT p.product_id, p.base_price
            FROM PRODUCT p
            """);

        if (categoryId != null) {
            sql.append(" JOIN PRODUCT_CATEGORY pc ON pc.product_id = p.product_id");
        }

        sql.append(" WHERE 1=1");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (searchIds != null) {
            sql.append(" AND p.product_id IN (:searchIds)");
            params.addValue("searchIds", searchIds);
        }
        if (minPrice != null) {
            sql.append(" AND p.base_price >= :minPrice");
            params.addValue("minPrice", minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.base_price <= :maxPrice");
            params.addValue("maxPrice", maxPrice);
        }
        if (categoryId != null) {
            sql.append(" AND pc.category_id = :categoryId");
            params.addValue("categoryId", categoryId);
        }

        if ("price_desc".equals(sortBy)) {
            sql.append(" ORDER BY p.base_price DESC");
        } else if ("price_asc".equals(sortBy)) {
            sql.append(" ORDER BY p.base_price ASC");
        } else {
            sql.append(" ORDER BY p.product_id");
        }

        sql.append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return jdbc.query(sql.toString(), params, productMapper);
    }
}