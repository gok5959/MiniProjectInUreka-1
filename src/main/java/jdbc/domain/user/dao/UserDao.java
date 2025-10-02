package jdbc.domain.user.dao;

import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.paging.Page;
import jdbc.domain.user.mapper.UserRowMapper;
import jdbc.domain.user.model.User;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private UserDao() {}

    private static class LazyHolder {
        private static final UserDao INSTANCE = new UserDao();
    }

    public static UserDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    public User findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return JdbcSupport.query(sql, rs -> {
            if (rs.next()) {
                return UserRowMapper.map(rs);
            }
            return null;
        }, id);
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT user_id, name, email, role, created_at, deleted_at FROM users";
        return JdbcSupport.query(sql, rs -> {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(UserRowMapper.map(rs));
            }
            return users;
        });
    }
    public Page<User> findInPage(int limit, int offset) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        Integer total = JdbcSupport.query(countSql, rs -> {
            rs.next();
            return rs.getInt(1);
        });

        String dataSql = """
                SELECT user_id, name, email, role, created_at, deleted_at
                FROM users
                ORDER BY user_id DESC
                LIMIT ? OFFSET ?
                """;

        List<User> content = JdbcSupport.query(dataSql, rs -> {
            List<User> list = new ArrayList<>();
            while(rs.next()) list.add(UserRowMapper.map(rs));
            return list;
        }, limit, offset);

        return new Page<>(content, total, limit, offset);
    }
    public int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (email, name, password, role) VALUES (?, ?, ?, ?)";
        return JdbcSupport.update(sql, user.getEmail(), user.getName(), user.getPassword(), user.getRole());
    }

    public int deleteUser(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return JdbcSupport.update(sql, id);
    }
}
