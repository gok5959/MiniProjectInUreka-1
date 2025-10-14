package jdbc.domain.order.dto;

import jdbc.domain.order.model.OrderState;
import jdbc.domain.order.model.ReviewStatus;

import java.time.LocalDateTime;

public record OrderSummary(
        Long orderId,
        Long productId,
        String productName,
        Long sellerId,
        String sellerName,
        Long buyerId,
        String buyerName,
        Integer priceAtOrder,
        OrderState orderState,
        ReviewStatus reviewStatus,
        LocalDateTime createdAt
) {
}
