package jdbc.domain.review.dao;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.paging.Page;
import jdbc.domain.review.dto.ReviewDto;
import jdbc.domain.review.mapper.ReviewRowMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private ReviewDao() {}

    private static class LazyHolder { private static final ReviewDao INSTANCE = new ReviewDao(); }
    public static ReviewDao getInstance() { return LazyHolder.INSTANCE; }

    public int insert(Long orderId, Long reviewerId, String title, String content, java.math.BigDecimal rating) {
        final String sql = "INSERT INTO reviews(order_id, reviewer_id, title, content, rating, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        try {
            return JdbcSupport.update(sql, orderId, reviewerId, title, content, rating);
        } catch (RuntimeException e) {
            throw new DataAccessException("insert review failed", e);
        }
    }

    public ReviewDto findById(Long reviewId) {
        final String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return JdbcSupport.query(sql, rs -> rs.next() ? ReviewRowMapper.map(rs) : null, reviewId);
        } catch (SQLException e) {
            throw new DataAccessException("findById failed", e);
        }
    }

    public Page<ReviewDto> findByProductId(Long productId, int limit, int offset) {
        // reviews table doesn't store product_id directly; join orders to filter by product
        final String countSql = "SELECT COUNT(*) FROM reviews r JOIN orders o ON r.order_id = o.order_id WHERE o.product_id = ?";
        final String dataSql = "SELECT r.* FROM reviews r JOIN orders o ON r.order_id = o.order_id WHERE o.product_id = ? ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        try {
            Integer total = JdbcSupport.query(countSql, rs -> { rs.next(); return rs.getInt(1); }, productId);
            List<ReviewDto> content = JdbcSupport.query(dataSql, rs -> {
                List<ReviewDto> list = new ArrayList<>();
                while (rs.next()) list.add(ReviewRowMapper.map(rs));
                return list;
            }, productId, limit, offset);
            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findByProductId failed", e);
        }
    }

    public ReviewDto findByOrderId(Long orderId) {
        final String sql = "SELECT * FROM reviews WHERE order_id = ? LIMIT 1";
        try {
            return JdbcSupport.query(sql, rs -> rs.next() ? ReviewRowMapper.map(rs) : null, orderId);
        } catch (SQLException e) {
            throw new DataAccessException("findByOrderId failed", e);
        }
    }

    public int update(Long reviewId, String title, String content, java.math.BigDecimal rating) {
        final String sql = "UPDATE reviews SET title = ?, content = ?, rating = ?, updated_at = NOW() WHERE review_id = ?";
        try {
            return JdbcSupport.update(sql, title, content, rating, reviewId);
        } catch (RuntimeException e) {
            throw new DataAccessException("update failed", e);
        }
    }

    public int delete(Long reviewId) {
        final String sql = "DELETE FROM reviews WHERE review_id = ?";
        try {
            return JdbcSupport.update(sql, reviewId);
        } catch (RuntimeException e) {
            throw new DataAccessException("delete failed", e);
        }
    }
}
