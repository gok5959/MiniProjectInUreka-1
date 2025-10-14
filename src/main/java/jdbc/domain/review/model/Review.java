package jdbc.domain.review.model;

import java.time.LocalDateTime;

public class Review {
    private Long reviewId;
    private Long orderId;
    private Long reviewerId; // buyer id
    private String title;
    private String content;
    private java.math.BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Review() {}

    public Review(Long reviewId, Long orderId, Long reviewerId, String title, String content, java.math.BigDecimal rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.reviewerId = reviewerId;
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return null; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public java.math.BigDecimal getRating() { return rating; }
    public void setRating(java.math.BigDecimal rating) { this.rating = rating; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
