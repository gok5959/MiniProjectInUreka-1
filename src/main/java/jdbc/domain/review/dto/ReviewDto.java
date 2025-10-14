package jdbc.domain.review.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record ReviewDto(
    Long reviewId,
    Long orderId,
    Long reviewerId,
    String title,
    String content,
    BigDecimal rating,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
