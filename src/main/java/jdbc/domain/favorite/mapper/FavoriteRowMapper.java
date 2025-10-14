package jdbc.domain.favorite.mapper;

import jdbc.domain.favorite.dto.FavoriteDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class FavoriteRowMapper {
    public static FavoriteDto map(ResultSet rs) throws SQLException {
        Long userId = rs.getLong("user_id");
        Long productId = rs.getLong("product_id");
        boolean active = rs.getInt("active") == 1;
        java.sql.Timestamp ca = rs.getTimestamp("created_at");
        java.sql.Timestamp ua = rs.getTimestamp("updated_at");
        LocalDateTime createdAt = ca == null ? null : ca.toLocalDateTime();
        LocalDateTime updatedAt = ua == null ? null : ua.toLocalDateTime();
        String productName = null;
        Long productPrice = null;
        try {
            productName = rs.getString("product_name");
        } catch (Exception ignored) {}
        try {
            Object p = rs.getObject("price");
            if (p != null) productPrice = ((Number) p).longValue();
        } catch (Exception ignored) {}
        return new FavoriteDto(userId, productId, active, createdAt, updatedAt, productName, productPrice);
    }
}
