package jdbc.domain.product.model;

import jdbc.domain.user.model.UserRole;

public enum ProductStatus {
    ON_SALE, RESERVED, SOLD, DELETED;

    public static ProductStatus fromString(String status) {
        if(status == null) return ON_SALE;
        try {
            return ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유저 Role이 정의되지 않았습니다. : " + status);
        }
    }
}
