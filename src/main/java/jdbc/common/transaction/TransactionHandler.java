package jdbc.common.transaction;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.DataSourceProvider;
import jdbc.common.jdbc.JdbcSupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class TransactionHandler {
    private TransactionHandler() {
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection con) throws Exception;
    }

    public static <T> T inTransaction(SqlFunction<T> work) {
        try (Connection con = DataSourceProvider.getConnection()) {
            boolean prev = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                T result = work.apply(con);
                con.commit();
                return result;
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException ignore) {
                    ignore.printStackTrace();
                }
                throw new DataAccessException("트랜잭션 실패", e);
            } finally {
                try {
                    con.setAutoCommit(prev);
                } catch (SQLException ignore) {
                    ignore.printStackTrace();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("커넥션 획득 실패", e);
        }
    }
}
