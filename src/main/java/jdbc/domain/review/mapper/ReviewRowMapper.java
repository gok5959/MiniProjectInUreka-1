package jdbc.domain.review.mapper;

import jdbc.domain.review.dto.ReviewDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ReviewRowMapper {
    public static ReviewDto map(ResultSet rs) throws SQLException {
        return new ReviewDto(
                rs.getLong("review_id"),
                rs.getLong("order_id"),
                rs.getLong("reviewer_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getBigDecimal("rating"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
