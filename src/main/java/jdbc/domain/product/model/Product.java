package jdbc.domain.product.model;

import java.time.LocalDateTime;

public class Product {
    private Long productId;
    private String name;
    private Long price;
    private Long categoryId;
    private Long sellerId;
    private ProductStatus status;
    private LocalDateTime createdAt;

    public Product(String name, Long price, Long categoryId, Long sellerId, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.status = status;
    }

    public Product(Long productId, String name, Long price, Long categoryId, Long sellerId, ProductStatus status) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.status = status;
    }

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Product(Long productId, String name, Long price, Long categoryId, Long sellerId, ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}
