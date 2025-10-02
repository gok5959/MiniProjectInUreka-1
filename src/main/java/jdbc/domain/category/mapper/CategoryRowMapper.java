package jdbc.domain.category.mapper;

import jdbc.domain.category.model.Category;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper {
    private CategoryRowMapper() {}

    public static Category map(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("category_id"),
                rs.getString("name"),
                rs.getObject("parent_id") == null ? null : rs.getLong("parent_id"));
    }

}
