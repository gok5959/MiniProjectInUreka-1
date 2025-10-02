package jdbc.domain.product.dao;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.JdbcSupport;

import java.sql.Connection;
import java.sql.SQLException;

public class ProductMetricsDao {
    private ProductMetricsDao() {
    }

    private static class LazyHolder {
        private static final ProductMetricsDao INSTANCE = new ProductMetricsDao();
    }

    public static ProductMetricsDao getInstance() {
        return LazyHolder.INSTANCE;
    }
    public void initIfAbsent(Long productId) {
        final String sql = """
            INSERT INTO product_metrics(product_id, like_count, view_count, updated_at)
            VALUES (?, 0, 0, NOW())
            ON DUPLICATE KEY UPDATE product_id = VALUES(product_id)
        """;
        JdbcSupport.update(sql, productId);
    }

    public void incrementView(Long productId) {
        final String sql = """
            UPDATE product_metrics
            SET view_count = view_count + 1, updated_at = NOW()
            WHERE product_id = ?
        """;
        JdbcSupport.update(sql, productId);
    }

    public void addLike(Long productId, long delta) {
        final String sql = """
            UPDATE product_metrics
            SET like_count = GREATEST(like_count + ?, 0), updated_at = NOW()
            WHERE product_id = ?
        """;
        JdbcSupport.update(sql, delta, productId);
    }

    public void initIfAbsent(Connection con, Long productId) {
        final String sql = """
        INSERT INTO product_metrics(product_id, like_count, view_count, updated_at)
        VALUES (?,0,0,NOW())
        ON DUPLICATE KEY UPDATE product_id = VALUES(product_id)
    """;
        JdbcSupport.update(con, sql, productId);
    }
    public void incrementView(Connection con, Long productId) {
        final String sql = "UPDATE product_metrics SET view_count=view_count+1, updated_at=NOW() WHERE product_id=?";
        JdbcSupport.update(con, sql, productId);
    }
    public void addLike(Connection con, Long productId, long delta) {
        final String sql = "UPDATE product_metrics SET like_count=GREATEST(like_count+?,0), updated_at=NOW() WHERE product_id=?";
        JdbcSupport.update(con, sql, delta, productId);
    }
}
