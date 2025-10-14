package jdbc.domain.favorite.dao;

import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.exception.DataAccessException;
import jdbc.domain.favorite.dto.FavoriteDto;
import jdbc.domain.favorite.mapper.FavoriteRowMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {
    private FavoriteDao() {}
    private static class Holder { static final FavoriteDao INSTANCE = new FavoriteDao(); }
    public static FavoriteDao getInstance() { return Holder.INSTANCE; }

    public FavoriteDto find(Long userId, Long productId) {
        String sql = "SELECT user_id, product_id, active, created_at, updated_at FROM favorites WHERE user_id=? AND product_id=?";
        try {
            return JdbcSupport.query(sql, rs -> rs.next() ? FavoriteRowMapper.map(rs) : null, userId, productId);
    } catch (SQLException e) { throw new DataAccessException(e.getMessage(), e); }
    }

    public FavoriteDto find(Connection con, Long userId, Long productId) {
        String sql = "SELECT user_id, product_id, active, created_at, updated_at FROM favorites WHERE user_id=? AND product_id=?";
        try {
            return JdbcSupport.query(con, sql, rs -> rs.next() ? FavoriteRowMapper.map(rs) : null, userId, productId);
    } catch (SQLException e) { throw new DataAccessException(e.getMessage(), e); }
    }

    public int insert(Connection con, Long userId, Long productId) {
        String sql = "INSERT INTO favorites(user_id, product_id, active, created_at) VALUES(?,?,1,NOW())";
        try {
            return JdbcSupport.update(con, sql, userId, productId);
        } catch (RuntimeException e) { throw new DataAccessException(e.getMessage(), e); }
    }

    public int updateActive(Connection con, Long userId, Long productId, boolean active) {
        String sql = "UPDATE favorites SET active=?, updated_at=NOW() WHERE user_id=? AND product_id=?";
        try {
            return JdbcSupport.update(con, sql, active?1:0, userId, productId);
        } catch (RuntimeException e) { throw new DataAccessException(e.getMessage(), e); }
    }

    public List<FavoriteDto> findByUser(Long userId, int limit, int offset) {
        String sql = "SELECT f.user_id, f.product_id, f.active, f.created_at, f.updated_at, p.name AS product_name, p.price FROM favorites f JOIN products p ON p.product_id = f.product_id WHERE f.user_id=? AND f.active=1 ORDER BY f.created_at DESC LIMIT ? OFFSET ?";
        try {
            return JdbcSupport.query(sql, rs -> { List<FavoriteDto> list = new ArrayList<>(); while (rs.next()) list.add(FavoriteRowMapper.map(rs)); return list; }, userId, limit, offset);
        } catch (SQLException e) { throw new DataAccessException(e.getMessage(), e); }
    }
}
