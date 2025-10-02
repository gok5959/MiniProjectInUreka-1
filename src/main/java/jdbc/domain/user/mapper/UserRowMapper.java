package jdbc.domain.user.mapper;

import jdbc.domain.user.model.User;
import jdbc.domain.user.model.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper {
    public static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("name"),
                rs.getString("password"),
                UserRole.fromString(rs.getString("role")),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }
}
