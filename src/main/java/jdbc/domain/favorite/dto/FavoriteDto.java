package jdbc.domain.favorite.dto;

import java.time.LocalDateTime;

public record FavoriteDto(Long userId, Long productId, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt, String productName, Long productPrice) {}
