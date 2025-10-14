// jdbc/domain/order/dao/OrderDao.java
package jdbc.domain.order.dao;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.paging.Page;
import jdbc.domain.order.dto.OrderSummary;
import jdbc.domain.order.mapper.OrderRowMapper;
import jdbc.domain.order.model.Order;
import jdbc.domain.order.model.OrderState;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private OrderDao() {
    }

    private static class LazyHolder {
        private static final OrderDao INSTANCE = new OrderDao();
    }

    public static OrderDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    // ---------- 단건 ----------
    public Order findById(Long orderId) {
        final String sql = "SELECT * FROM orders WHERE order_id = ?";
        try {
            return JdbcSupport.query(sql, rs -> {
                if (rs.next()) {
                    return OrderRowMapper.map(rs);
                }
                return null;
            }, orderId);
        } catch (SQLException e) {
            throw new DataAccessException("findById failed. id=" + orderId, e);
        }
    }

    public Order findById(Connection con, Long orderId) {
        final String sql = "SELECT * FROM orders WHERE order_id = ?";
        try {
            return JdbcSupport.query(con, sql, rs -> {
                if (rs.next()) {
                    return OrderRowMapper.map(rs);
                }
                return null;
            }, orderId);
        } catch (SQLException e) {
            throw new DataAccessException("findById(tx) failed. id=" + orderId, e);
        }
    }

    public Order findActiveByProductId(Connection con, Long productId) {
        final String sql = """
                    SELECT * FROM orders
                    WHERE product_id = ?
                      AND order_state IN ('REQUESTED','ACCEPTED')
                    ORDER BY order_id DESC
                    LIMIT 1
                """;
        try {
            return JdbcSupport.query(con, sql, rs -> {
                if (rs.next()) {
                    System.out.println(OrderRowMapper.map(rs));
                    return OrderRowMapper.map(rs);
                }
                return null;
            }, productId);
        } catch (SQLException e) {
            throw new DataAccessException("findActiveByProductId failed. productId=" + productId, e);
        }
    }

    // ---------- 생성/전이 ----------
    public long insertRequested(Connection con, Long productId, Long buyerId, int priceAtOrder) {
        final String sql = """
                    INSERT INTO orders(product_id, buyer_id, price_at_order, order_state, review_status, created_at)
                    VALUES (?, ?, ?, 'REQUESTED', 'BEFORE_REVIEW', NOW())
                """;
        return JdbcSupport.update(con, sql, productId, buyerId, priceAtOrder);

    }

    public int updateState(Connection con, Long orderId, OrderState expected, OrderState next) {
        final String sql = "UPDATE orders SET order_state = ? WHERE order_id = ? AND order_state = ?";
        return JdbcSupport.update(con, sql, next.name(), orderId, expected.name());
    }

    public int markReviewed(Connection con, Long orderId) {
        final String sql = "UPDATE orders SET review_status = 'REVIEWED' WHERE order_id = ?";
        return JdbcSupport.update(con, sql, orderId);
    }

    // ---------- 조회(사용자/관리자) ----------

    /**
     * 구매자 관점 조회
     */
    public Page<OrderSummary> findBuyerPage(Long buyerId, int limit, int offset, OrderState state) {
        StringBuilder where = new StringBuilder(" WHERE o.buyer_id = ? ");
        List<Object> paramsCount = new ArrayList<>();
        List<Object> paramsData = new ArrayList<>();
        paramsCount.add(buyerId);
        paramsData.add(buyerId);

        if (state != null) {
            where.append(" AND o.order_state = ? ");
            paramsCount.add(state.name());
            paramsData.add(state.name());
        }

        final String countSql = "SELECT COUNT(*) FROM orders o" + where;
        final String dataSql = """
                    SELECT
                      o.order_id, o.product_id, p.name AS product_name,
                      o.buyer_id, u.name AS buyer_name,
                      o.price_at_order, o.order_state, o.review_status, o.created_at
                    FROM orders o
                    JOIN products p ON p.product_id = o.product_id
                    JOIN users    u ON u.user_id    = o.buyer_id
                """ + where + """
                    ORDER BY o.created_at DESC, o.order_id DESC
                    LIMIT ? OFFSET ?
                """;

        try {
            Integer total = JdbcSupport.query(countSql, rs -> {
                rs.next();
                return rs.getInt(1);
            }, paramsCount.toArray());

            paramsData.add(limit);
            paramsData.add(offset);

            List<OrderSummary> content = JdbcSupport.query(dataSql, rs -> {
                List<OrderSummary> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new OrderSummary(
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getLong("buyer_id"),
                            rs.getString("buyer_name"),
                            rs.getInt("price_at_order"),
                            OrderState.fromDb(rs.getString("order_state")),
                            jdbc.domain.order.model.ReviewStatus.fromDb(rs.getString("review_status")),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
                return list;
            }, paramsData.toArray());

            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findBuyerPage failed", e);
        }
    }

    /**
     * 판매자 관점 조회: 내 상품들에 대한 주문
     */
    public Page<OrderSummary> findSellerPage(Long sellerId, int limit, int offset, OrderState state) {
        StringBuilder where = new StringBuilder(" WHERE p.seller_id = ? ");
        List<Object> paramsCount = new ArrayList<>();
        List<Object> paramsData = new ArrayList<>();
        paramsCount.add(sellerId);
        paramsData.add(sellerId);

        if (state != null) {
            where.append(" AND o.order_state = ? ");
            paramsCount.add(state.name());
            paramsData.add(state.name());
        }

        final String countSql = """
                    SELECT COUNT(*)
                    FROM orders o
                    JOIN products p ON p.product_id = o.product_id
                """ + where;

        final String dataSql = """
                    SELECT
                      o.order_id, o.product_id, p.name AS product_name,
                      o.buyer_id, u.name AS buyer_name,
                      o.price_at_order, o.order_state, o.review_status, o.created_at
                    FROM orders o
                    JOIN products p ON p.product_id = o.product_id
                    JOIN users    u ON u.user_id    = o.buyer_id
                """ + where + """
                    ORDER BY o.created_at DESC, o.order_id DESC
                    LIMIT ? OFFSET ?
                """;

        try {
            Integer total = JdbcSupport.query(countSql, rs -> {
                rs.next();
                return rs.getInt(1);
            }, paramsCount.toArray());

            paramsData.add(limit);
            paramsData.add(offset);

            List<OrderSummary> content = JdbcSupport.query(dataSql, rs -> {
                List<OrderSummary> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new OrderSummary(
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getLong("buyer_id"),
                            rs.getString("buyer_name"),
                            rs.getInt("price_at_order"),
                            OrderState.fromDb(rs.getString("order_state")),
                            jdbc.domain.order.model.ReviewStatus.fromDb(rs.getString("review_status")),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
                return list;
            }, paramsData.toArray());

            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findSellerPage failed", e);
        }
    }

    /**
     * 관리자 관점 조회: 필터(상태/구매자/상품)
     */
    public Page<OrderSummary> findAdminPage(int limit, int offset, OrderState state, Long buyerId, Long productId) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> paramsCount = new ArrayList<>();
        List<Object> paramsData = new ArrayList<>();

        if (state != null) {
            where.append(" AND o.order_state = ? ");
            paramsCount.add(state.name());
            paramsData.add(state.name());
        }
        if (buyerId != null) {
            where.append(" AND o.buyer_id = ? ");
            paramsCount.add(buyerId);
            paramsData.add(buyerId);
        }
        if (productId != null) {
            where.append(" AND o.product_id = ? ");
            paramsCount.add(productId);
            paramsData.add(productId);
        }

        final String countSql = "SELECT COUNT(*) FROM orders o" + where;
        final String dataSql = """
                    SELECT
                      o.order_id, o.product_id, p.name AS product_name,
                      o.buyer_id, u.name AS buyer_name,
                      o.price_at_order, o.order_state, o.review_status, o.created_at
                    FROM orders o
                    JOIN products p ON p.product_id = o.product_id
                    JOIN users    u ON u.user_id    = o.buyer_id
                """ + where + """
                    ORDER BY o.created_at DESC, o.order_id DESC
                    LIMIT ? OFFSET ?
                """;

        try {
            Integer total = JdbcSupport.query(countSql, rs -> {
                rs.next();
                return rs.getInt(1);
            }, paramsCount.toArray());

            paramsData.add(limit);
            paramsData.add(offset);

            List<OrderSummary> content = JdbcSupport.query(dataSql, rs -> {
                List<OrderSummary> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new OrderSummary(
                            rs.getLong("order_id"),
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getLong("buyer_id"),
                            rs.getString("buyer_name"),
                            rs.getInt("price_at_order"),
                            OrderState.fromDb(rs.getString("order_state")),
                            jdbc.domain.order.model.ReviewStatus.fromDb(rs.getString("review_status")),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
                return list;
            }, paramsData.toArray());

            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findAdminPage failed", e);
        }
    }


}
