package jdbc.domain.product.service;

import jdbc.common.exception.DataAccessException;
import jdbc.common.paging.Page;
import jdbc.common.transaction.TransactionHandler;
import jdbc.domain.product.dao.ProductDao;
import jdbc.domain.product.dao.ProductMetricsDao;
import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.model.ProductWithMetrics;


public class ProductService {
    private ProductDao productDao;
    private ProductMetricsDao productMetricsDao;

    public ProductService() {
        this.productDao = ProductDao.getInstance();
        this.productMetricsDao = ProductMetricsDao.getInstance();
    }
    public Page<ProductWithMetrics> getPage(int limit, int offset) {
        return productDao.findPageWithMetric(limit, offset);
    }

    public Page<ProductWithMetrics> searchPage(
            int limit, int offset,
            String nameLike, Long categoryId, Long sellerId, ProductStatus status
    ) {
        return productDao.findSearchPageWithMetric(limit, offset, nameLike, categoryId, sellerId, status);
    }

    public int create(Product p) {
        // metrics는 최초 조회/좋아요 때 initIfAbsent 실행해도 충분
        return productDao.insert(p);
    }

    public int update(Product p) {
        return productDao.update(p);
    }

    /** 소프트 삭제 (deleted_at 세팅) */
    public boolean delete(Long productId) {
        return productDao.softDelete(productId) == 1;
    }

    public int updateStatus(Long productId, ProductStatus status) {
        return productDao.updateStatus(productId, status);
    }

    public ProductWithMetrics getDetailAndAddView(Long productId) {
        return TransactionHandler.inTransaction(con -> {
            if (!productDao.exists(con, productId)) throw new DataAccessException("Product not found. id=" + productId);
            productMetricsDao.initIfAbsent(con, productId);
            productMetricsDao.incrementView(con, productId);
            return productDao.findWithSellerAndMetricsById(con, productId);
        });
    }
    public ProductWithMetrics like(Long productId) {
        return TransactionHandler.inTransaction(con -> {
            if (!productDao.exists(con, productId)) throw new DataAccessException("Product not found. id=" + productId);
            productMetricsDao.initIfAbsent(con, productId);
            productMetricsDao.addLike(con, productId, +1);
            return productDao.findWithSellerAndMetricsById(con, productId);
        });
    }
    public ProductWithMetrics unlike(Long productId) {
        return TransactionHandler.inTransaction(con -> {
            if (!productDao.exists(con, productId)) throw new DataAccessException("Product not found. id=" + productId);
            productMetricsDao.initIfAbsent(con, productId);
            productMetricsDao.addLike(con, productId, -1);
            return productDao.findWithSellerAndMetricsById(con, productId);
        });
    }


}
