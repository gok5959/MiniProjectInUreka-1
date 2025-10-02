package test;

import jdbc.common.jdbc.JdbcSupport;
import jdbc.common.paging.Page;
import jdbc.domain.product.dao.ProductDao;
import jdbc.domain.product.dao.ProductMetricsDao;
import jdbc.domain.product.dto.SellerSummary;
import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.model.ProductWithMetrics;

import java.sql.SQLException;

public class ProductDaoTest {
    private static final ProductDao productDao = ProductDao.getInstance();
    private static final ProductMetricsDao metricsDao = ProductMetricsDao.getInstance();

    public static void main(String[] args) {
        try {
            System.out.println("=== ProductDao / ProductMetricsDao Manual Smoke Test ===");

            Long anySellerId = pickAnySellerId();
            if (anySellerId == null) {
                System.out.println("[중단] users 테이블에 판매자 데이터가 없습니다. 최소 1명 추가 후 다시 실행하세요.");
                return;
            }
            System.out.println("사용할 sellerId = " + anySellerId);

            // 1) 페이징 전체
            System.out.println("\n[1] 페이징 전체 리스트 (limit=5, offset=0)");
            Page<ProductWithMetrics> page1 = productDao.findPageWithMetric(5, 0);
            printPage(page1);

            // 2) 검색 페이징 (nameLike)
            System.out.println("\n[2] 검색 페이징 (nameLike='Phone')");
            Page<ProductWithMetrics> pageSearch = productDao.findSearchPageWithMetric(10, 0, "Phone", null, null, null);
            printPage(pageSearch);

            // 3) INSERT
            System.out.println("\n[3] INSERT");
            String tmpName = "ManualRunnerCam-" + System.currentTimeMillis();
            Long newId = insertProduct(tmpName, 123000L, 5L, ProductStatus.ON_SALE, anySellerId);
            System.out.println("생성된 product 후보명 = " + tmpName + ", 추정 product_id = " + newId);

            // 4) UPDATE (가격/상태/판매자 변경)
            System.out.println("\n[4] UPDATE (price, status, seller)");
            Product toUpdate = new Product(newId, tmpName, 99000L, 7L, anySellerId, ProductStatus.RESERVED);
            int rows = productDao.update(toUpdate);
            System.out.println("update rows = " + rows);

            // 5) 상태만 변경
            System.out.println("\n[5] updateStatus -> SOLD");
            rows = productDao.updateStatus(newId, ProductStatus.SOLD);
            System.out.println("updateStatus rows = " + rows);

            // 6) 메트릭: initIfAbsent + view 증가 + like 증감
            System.out.println("\n[6] Metrics: init + view + like");
            metricsDao.initIfAbsent(newId);
            metricsDao.incrementView(newId);
            metricsDao.incrementView(newId);
            metricsDao.addLike(newId, +3);
            metricsDao.addLike(newId, -5); // 바닥 0 확인용
            long[] mv = readMetrics(newId);
            System.out.println("metrics(after): like=" + mv[0] + ", view=" + mv[1]);

            // 7) 검색으로 방금 상품 확인
            System.out.println("\n[7] 검색으로 방금 상품 확인 (nameLike='" + tmpName + "')");
            Page<ProductWithMetrics> pageNew = productDao.findSearchPageWithMetric(5, 0, tmpName, null, null, null);
            printPage(pageNew);

            // 8) 소프트 삭제
            System.out.println("\n[8] softDelete");
            rows = productDao.softDelete(newId);
            System.out.println("softDelete rows = " + rows);

            // 9) 삭제 후 검색 (0건 기대)
            System.out.println("\n[9] 삭제 후 검색 (0건 기대)");
            Page<ProductWithMetrics> pageAfterDelete = productDao.findSearchPageWithMetric(5, 0, tmpName, null, null, null);
            printPage(pageAfterDelete);

            System.out.println("\n=== 완료 ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- helpers ---

    /** users 테이블에서 임의의 판매자 한 명 선택 */
    private static Long pickAnySellerId() throws SQLException {
        return JdbcSupport.query(
                "SELECT user_id FROM users WHERE deleted_at IS NULL OR deleted_at IS NULL IS NULL ORDER BY user_id ASC LIMIT 1",
                rs -> rs.next() ? rs.getLong(1) : null
        );
    }

    /** 제품 INSERT 후 해당 세션 기반으로 LAST_INSERT_ID()를 쓸 수 없으므로, name+seller로 최신행 추정 */
    private static Long insertProduct(String name, long price, Long categoryId, ProductStatus status, Long sellerId) {
        Product p = new Product(name, price, categoryId, sellerId, status);
        int rows = productDao.insert(p);
        System.out.println("insert rows = " + rows);

        // 가장 최근 생성된 동일 (name, seller) 검색
        try {
            return JdbcSupport.query("""
                    SELECT product_id
                    FROM products
                    WHERE name = ? AND seller_id = ?
                    ORDER BY product_id DESC
                    LIMIT 1
                """, rs -> rs.next() ? rs.getLong(1) : null, name, sellerId);
        } catch (SQLException e) {
            throw new RuntimeException("insertProduct 후 id 조회 실패", e);
        }
    }

    /** metrics 직접 읽어서 like/view 반환 */
    private static long[] readMetrics(Long productId) throws SQLException {
        return JdbcSupport.query("SELECT like_count, view_count FROM product_metrics WHERE product_id = ?",
                rs -> {
                    if (!rs.next()) return new long[]{0, 0};
                    return new long[]{rs.getLong(1), rs.getLong(2)};
                }, productId);
    }

    private static void printPage(Page<ProductWithMetrics> page) {
        System.out.printf("total=%d, limit=%d, offset=%d, totalPages=%d, currentPage=%d%n",
                page.total(), page.limit(), page.offset(), page.totalPages(), page.currentPage());
        for (ProductWithMetrics p : page.content()) {
            SellerSummary s = p.seller();
            System.out.printf("- #%d %s | %,d원 | cat=%s | status=%s | like=%d, view=%d | seller=%s(%s)%n",
                    p.productId(),
                    p.name(),
                    p.price(),
                    String.valueOf(p.categoryId()),
                    String.valueOf(p.status()),
                    p.likeCount(),
                    p.viewCount(),
                    s == null ? "?" : s.sellerName(),
                    s == null ? "?" : s.sellerEmail()
            );
        }
    }
}
