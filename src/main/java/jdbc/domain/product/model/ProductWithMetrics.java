package jdbc.domain.product.model;

import jdbc.domain.product.dto.SellerSummary;

public record ProductWithMetrics(
        Long productId, String name, Long price, Long categoryId, ProductStatus status,
        long likeCount, long viewCount,
        SellerSummary seller
) {

}
