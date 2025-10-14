package jdbc.domain.order.model;

import java.time.LocalDateTime;

public class Order {
    private Long orderId;
    private Long productId;
    private Long buyerId;
    private Integer priceAtOrder;
    private OrderState orderState;
    private ReviewStatus reviewStatus;
    private LocalDateTime createdAt;

    public Order(){}
    public Order(Long orderId, Long productId, Long buyerId, Integer priceAtOrder, OrderState orderState, ReviewStatus reviewStatus, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.priceAtOrder = priceAtOrder;
        this.orderState = orderState;
        this.reviewStatus = reviewStatus;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Integer priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
