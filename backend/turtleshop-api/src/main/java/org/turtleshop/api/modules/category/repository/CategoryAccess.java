package org.turtleshop.api.modules.category.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.turtleshop.api.modules.category.model.CategoryModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryAccess {
    private final NamedParameterJdbcTemplate jdbc;

    // Map JDBC rows to category model objects
    private final RowMapper<CategoryModel> categoryMapper = (rs, rowNum) -> {
        CategoryModel category = new CategoryModel();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    };

    public List<CategoryModel> findAll() {
        String sql = "SELECT category_id, name, description FROM CATEGORY";
        return jdbc.query(sql, categoryMapper);
    }

    // Find a category by id
    public Optional<CategoryModel> findById(int id) {
        String sql = "SELECT category_id, name, description FROM CATEGORY WHERE category_id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", id), categoryMapper)
                .stream()
                .findFirst();
    }

    // Insert a new category and return its generated id
    public int insert(CategoryModel category) {
        int nextId = jdbc.queryForObject("SELECT COALESCE(MAX(category_id), 0) + 1 FROM CATEGORY",
                new MapSqlParameterSource(), Integer.class);
        category.setCategoryId(nextId);

        String sql = """
            INSERT INTO CATEGORY (category_id, name, description)
            VALUES (:id, :name, :description)
        """;

        jdbc.update(sql, getParameters(category));
        return nextId;
    }

    // Update an existing category by id
    public void update(CategoryModel category) {
        String sql = """
            UPDATE CATEGORY
            SET name = :name,
                description = :description
            WHERE category_id = :id
        """;

        jdbc.update(sql, getParameters(category));
    }

    // Remove a category by id
    public void deleteById(int id) {
        jdbc.update("DELETE FROM CATEGORY WHERE category_id = :id",
                new MapSqlParameterSource("id", id));
    }

    // Build JDBC parameters from category object
    private MapSqlParameterSource getParameters(CategoryModel category) {
        return new MapSqlParameterSource()
                .addValue("id", category.getCategoryId())
                .addValue("name", category.getName())
                .addValue("description", category.getDescription());
    }
}
