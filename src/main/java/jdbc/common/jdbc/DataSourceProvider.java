package jdbc.common.jdbc;

import jdbc.common.config.DbConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceProvider {
    private static final DbConfig config = new DbConfig();

    private DataSourceProvider() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}
