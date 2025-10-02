package jdbc.domain.product.mapper;

import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper {
    private ProductRowMapper() {
    }

    public static Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getLong("Price"),
                rs.getLong("category_id"),
                rs.getLong("seller_id"),
                ProductStatus.fromString(rs.getString("status")),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toLocalDateTime());

    }
}
