import jdbc.common.jdbc.JdbcSupport;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String sql = "SELECT * FROM users";
        JdbcSupport.query(sql, rs -> {
            while(rs.next()) {
                System.out.println(rs.getString("name"));
            }
            return null;
        });
    }
}