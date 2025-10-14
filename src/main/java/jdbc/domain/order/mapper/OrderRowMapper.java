package jdbc.domain.order.mapper;

import jdbc.domain.order.model.Order;
import jdbc.domain.order.model.OrderState;
import jdbc.domain.order.model.ReviewStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OrderRowMapper {
    private OrderRowMapper(){}
    public static Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getLong("order_id"));
        o.setProductId(rs.getLong("product_id"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setPriceAtOrder(rs.getInt("price_at_order"));
        o.setOrderState(OrderState.fromDb(rs.getString("order_state")));
        o.setReviewStatus(ReviewStatus.fromDb(rs.getString("review_status")));
        Timestamp ts = rs.getTimestamp("created_at");
        o.setCreatedAt(ts==null?null:ts.toLocalDateTime());
        return o;
    }
}
