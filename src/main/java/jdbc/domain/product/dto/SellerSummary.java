package jdbc.domain.product.dto;

public record SellerSummary(
        Long sellerId,
        String sellerName,
        String sellerEmail,
        String sellerRole) {
}
