package jdbc.domain.category.dao;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.JdbcSupport;
import jdbc.domain.category.mapper.CategoryRowMapper;
import jdbc.domain.category.model.Category;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private CategoryDao() {
    }

    private static class LazyHolder {
        private static final CategoryDao INSTANCE = new CategoryDao();
    }

    public static CategoryDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Category findCategoryById(Long id) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try {
            return JdbcSupport.query(sql, rs -> rs.next() ? CategoryRowMapper.map(rs) : null);
        } catch(SQLException e) {
            throw new DataAccessException("findById in category failed. id = " + id, e);
        }
    }

    public List<Category> findCategoryAll() {
        String sql = "SELECT * FROM categories";
        try {
            return JdbcSupport.query(sql, rs -> {
                List<Category> list = new ArrayList<>();
                while(rs.next()) {
                    list.add(CategoryRowMapper.map(rs));
                }
                return list;
            });
        } catch(SQLException e) {
            throw new DataAccessException("findAll in category failed", e);
        }
    }

    public List<Category> findAllOrdered() {
        // 부모 -> 자식 순으로 정렬 (형제는 category_id 오름차순)
        final String sql = """
        WITH RECURSIVE cat AS (
            SELECT c.category_id, c.name, c.parent_id, ARRAY[c.category_id] AS path
            FROM categories c
            WHERE c.parent_id IS NULL
            UNION ALL
            SELECT ch.category_id, ch.name, ch.parent_id, cat.path || ch.category_id
            FROM categories ch
            JOIN cat ON ch.parent_id = cat.category_id
        )
        SELECT category_id, name, parent_id
        FROM cat
        ORDER BY path
        """;
        try {
            return JdbcSupport.query(sql, rs -> {
                List<Category> list = new ArrayList<>();
                while (rs.next()) list.add(CategoryRowMapper.map(rs));
                return list;
            });
        } catch (SQLException e) {
            throw new DataAccessException("findAllOrdered failed.", e);
        }
    }

    public int insertCategory(Category category) {
        String sql = "INSERT INTO categories(category_id, category_name) VALUES(?, ?)";
        return JdbcSupport.update(sql, category.getName(), category.getParentId());
    }

    public int renameCategory(Long id, String newName) {
        String sql = "UPDATE categories SET category_name = ? WHERE category_id = ?";
        return JdbcSupport.update(sql, id, newName);
    }

    public int delete(Long id) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        return JdbcSupport.update(sql, id);
    }
}
