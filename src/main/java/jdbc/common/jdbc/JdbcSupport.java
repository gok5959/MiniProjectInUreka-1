package jdbc.common.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcSupport {

    private JdbcSupport() {
    }

    public static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    public interface ResultSetExtractor<T> {
        T extract(ResultSet rs) throws SQLException;
    }

    public static <T> T query(String sql, ResultSetExtractor<T> extractor, Object... params) throws SQLException {
        try (Connection conn = DataSourceProvider.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return extractor.extract(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("쿼리 실행 실패 : " + sql, e);
        }
    }

    public static int update(String sql, Object... params) {
        try (Connection conn = DataSourceProvider.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("업데이트 실행 실패 : " + sql, e);
        }
    }

    public static <T> T query(Connection conn, String sql, ResultSetExtractor<T> extractor, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return extractor.extract(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("쿼리 실행 실패 : " + sql, e);
        }
    }

    public static int update(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("업데이트 실행 실패 : " + sql +  e.getMessage());
            throw new RuntimeException("업데이트 실행 실패 : " + sql, e);
        }
    }
}
