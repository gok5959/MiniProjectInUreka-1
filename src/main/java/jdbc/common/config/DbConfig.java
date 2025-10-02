package jdbc.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfig {
    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    public DbConfig() {
        Properties props = new Properties();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("db.properties 로딩 실패", e);
        }

        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/mp1");
        this.username = props.getProperty("db.username", "root");
        this.password = props.getProperty("db.password", "");
        this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC DRIVER 로딩 실패", e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
