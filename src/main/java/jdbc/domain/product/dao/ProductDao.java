package jdbc.domain.product.dao;

import jdbc.common.exception.DataAccessException;
import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.paging.Page;
import jdbc.domain.product.dto.SellerSummary;
import jdbc.domain.product.mapper.ProductRowMapper;
import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.model.ProductWithMetrics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private ProductDao() {
    }

    private static class LazyHolder {
        private static final ProductDao INSTANCE = new ProductDao();
    }

    public static ProductDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Product findById(Connection con, Long id) throws SQLException {
        final String sql = """
                SELECT * FROM products WHERE product_id = ?
                """;
        return JdbcSupport.query(con, sql, rs -> {
            if (rs.next()) {
                return ProductRowMapper.map(rs);
            } else {
                throw new SQLException("Product with id " + id + " not found");
            }
        }, id);
    }

    public Page<ProductWithMetrics> findPageWithMetric(int limit, int offset) {
        final String countSql = """
                SELECT COUNT(*) FROM products p
                WHERE p.deleted_at IS NULL
                """;
        final String dataSql = """
                    SELECT
                      p.product_id, p.name, p.price, p.category_id, p.status, p.seller_id,
                      COALESCE(m.like_count,0) AS like_count,
                      COALESCE(m.view_count,0) AS view_count,
                      u.name  AS seller_name,
                      u.email AS seller_email,
                      u.role  AS seller_role
                    FROM products p
                    JOIN users u ON u.user_id = p.seller_id
                    LEFT JOIN product_metrics m ON m.product_id = p.product_id
                    WHERE p.deleted_at IS NULL
                    LIMIT ? OFFSET ?
                """;

        try {
            Integer total = JdbcSupport.query(countSql, rs -> {
                rs.next();
                return rs.getInt(1);
            });
            List<ProductWithMetrics> content = JdbcSupport.query(dataSql, rs -> {
                List<ProductWithMetrics> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ProductWithMetrics(
                            rs.getLong("product_id"),
                            rs.getString("name"),
                            rs.getLong("price"),
                            rs.getLong("category_id"),
                            rs.getString("status") == null ? null : ProductStatus.valueOf(rs.getString("status")),
                            rs.getLong("like_count"),
                            rs.getLong("view_count"),
                            new SellerSummary(
                                    rs.getLong("seller_id"),
                                    rs.getString("seller_name"),
                                    rs.getString("seller_email"),
                                    rs.getString("seller_role")
                            )
                    ));
                }
                return list;
            }, limit, offset);

            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findPageWithMetric failed.", e);
        }

    }

    public Page<ProductWithMetrics> findSearchPageWithMetric(int limit, int offset, String nameLike, Long categoryId, Long sellerId, ProductStatus status) {
        StringBuilder where = new StringBuilder(" WHERE p.deleted_at IS NULL ");
        List<Object> countParams = new ArrayList<>();
        List<Object> dataParams = new ArrayList<>();

        if (nameLike != null && !nameLike.isBlank()) {
            where.append(" AND p.name LIKE ? ");
            String like = "%" + nameLike + "%";
            countParams.add(like);
            dataParams.add(like);
        }

        if (categoryId != null) {
            where.append(" AND p.category_id = ? ");
            countParams.add(categoryId);
            dataParams.add(categoryId);
        }
        if (sellerId != null) {
            where.append(" AND p.seller_id = ? ");
            countParams.add(sellerId);
            dataParams.add(sellerId);
        }
        if (status != null) {
            where.append(" AND p.status = ? ");
            countParams.add(status.name());
            dataParams.add(status.name());
        }

        final String countSql = "SELECT COUNT(*) FROM products p" + where;
        final String dataSql = """
                    SELECT
                      p.product_id, p.name, p.price, p.category_id, p.status, p.seller_id,
                      COALESCE(m.like_count,0) AS like_count,
                      COALESCE(m.view_count,0) AS view_count,
                      u.name  AS seller_name,
                      u.email AS seller_email,
                      u.role  AS seller_role
                    FROM products p
                    JOIN users u ON u.user_id = p.seller_id
                    LEFT JOIN product_metrics m ON m.product_id = p.product_id
                """ + where + """
                    LIMIT ? OFFSET ?
                """;

        try {
            Integer total = JdbcSupport.query(countSql, rs -> {
                rs.next();
                return rs.getInt(1);
            }, countParams.toArray());

            dataParams.add(limit);
            dataParams.add(offset);
            List<ProductWithMetrics> content = JdbcSupport.query(dataSql, rs -> {
                List<ProductWithMetrics> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new ProductWithMetrics(
                            rs.getLong("product_id"),
                            rs.getString("name"),
                            rs.getLong("price"),
                            (Long) rs.getObject("category_id"),
                            rs.getString("status") == null ? null : ProductStatus.valueOf(rs.getString("status")),
                            rs.getLong("like_count"),
                            rs.getLong("view_count"),
                            new SellerSummary(
                                    (Long) rs.getObject("seller_id"),
                                    rs.getString("seller_name"),
                                    rs.getString("seller_email"),
                                    rs.getString("seller_role")
                            )
                    ));
                }
                return list;
            }, dataParams.toArray());
            return new Page<>(content, total, limit, offset);
        } catch (SQLException e) {
            throw new DataAccessException("findSearchPageWithMetrics failed." + e);
        }
    }

    public int insert(Product p) {
        final String sql = """
                    INSERT INTO products (name, price, category_id, status, seller_id, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                """;
        return JdbcSupport.update(sql,
                p.getName(),
                p.getPrice(),
                p.getCategoryId(),
                p.getStatus() == null ? null : p.getStatus().name(),
                p.getSellerId()
        );
    }

    public int update(Product p) {
        final String sql = """
                    UPDATE products
                    SET name = ?, price = ?, category_id = ?, status = ?, seller_id = ?, updated_at = NOW()
                    WHERE product_id = ? AND deleted_at IS NULL
                """;
        return JdbcSupport.update(sql,
                p.getName(),
                p.getPrice(),
                p.getCategoryId(),
                p.getStatus() == null ? null : p.getStatus().name(),
                p.getSellerId(),
                p.getProductId()
        );
    }

    public int softDelete(Long productId) {
        final String sql = """
                    UPDATE products
                    SET deleted_at = NOW(), updated_at = NOW()
                    WHERE product_id = ? AND deleted_at IS NULL
                """;
        return JdbcSupport.update(sql, productId);
    }

    public int updateStatus(Long productId, ProductStatus status) {
        final String sql = """
                    UPDATE products
                    SET status = ?, updated_at = NOW()
                    WHERE product_id = ? AND deleted_at IS NULL
                """;
        return JdbcSupport.update(sql, status.name(), productId);
    }

    public boolean exists(Connection con, Long productId) {
        final String sql = "SELECT 1 FROM products WHERE product_id=? AND deleted_at IS NULL";
        try {
            return JdbcSupport.query(con, sql, rs -> rs.next(), productId);
        } catch (SQLException e) {
            throw new DataAccessException("exists failed. id=" + productId, e);
        }
    }

    public ProductWithMetrics findWithSellerAndMetricsById(Connection con, Long id) {
        final String sql = """
                    SELECT
                      p.product_id, p.name, p.price, p.category_id, p.status, p.seller_id,
                      COALESCE(m.like_count,0) AS like_count,
                      COALESCE(m.view_count,0) AS view_count,
                      u.name  AS seller_name,
                      u.email AS seller_email,
                      u.role  AS seller_role
                    FROM products p
                    JOIN users u ON u.user_id = p.seller_id
                    LEFT JOIN product_metrics m ON m.product_id = p.product_id
                    WHERE p.product_id = ? AND p.deleted_at IS NULL
                """;
        try {
            return JdbcSupport.query(con, sql, rs -> {
                if (!rs.next()) return null;
                return new ProductWithMetrics(
                        rs.getLong("product_id"),
                        rs.getString("name"),
                        rs.getLong("price"),
                        rs.getLong("category_id"),
                        ProductStatus.fromString(rs.getString("status")), // enum 헬퍼 사용 권장
                        rs.getLong("like_count"),
                        rs.getLong("view_count"),
                        new SellerSummary(
                                (Long) rs.getObject("seller_id"),
                                rs.getString("seller_name"),
                                rs.getString("seller_email"),
                                rs.getString("seller_role")
                        )
                );
            }, id);
        } catch (SQLException e) {
            throw new DataAccessException("findWithSellerAndMetricsById failed. id=" + id, e);
        }
    }

    // ProductDao에 추가 (Connection 오버로드 사용)
    public int reserveIfOnSale(Connection con, Long productId) {
        final String sql = """
        UPDATE products
        SET status = 'RESERVED', updated_at = NOW()
        WHERE product_id = ? AND deleted_at IS NULL AND status = 'ON_SALE'
    """;
        try {
            return JdbcSupport.update(con, sql, productId);
        } catch (DataAccessException e) {
            throw new DataAccessException("reserveIfOnSale failed. id=" + productId, e);
        }
    }

    public int setOnSaleIfReserved(Connection con, Long productId) {
        final String sql = """
        UPDATE products
        SET status = 'ON_SALE', updated_at = NOW()
        WHERE product_id = ? AND deleted_at IS NULL AND status = 'RESERVED'
    """;
        try {
            return JdbcSupport.update(con, sql, productId);
        } catch (DataAccessException e) {
            throw new DataAccessException("setOnSaleIfReserved failed. id=" + productId, e);
        }
    }

    public int setSoldIfReserved(Connection con, Long productId) {
        final String sql = """
        UPDATE products
        SET status = 'SOLD', updated_at = NOW()
        WHERE product_id = ? AND deleted_at IS NULL AND status = 'RESERVED'
    """;
        try {
            return JdbcSupport.update(con, sql, productId);
        } catch (DataAccessException e) {
            throw new DataAccessException("setSoldIfReserved failed. id=" + productId, e);
        }
    }
}
