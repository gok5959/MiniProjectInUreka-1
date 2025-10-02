package test;// src/main/java/manual/ProductServiceManualRunner.java

import jdbc.common.paging.Page;
import jdbc.common.jdbc.JdbcSupport;
import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;
import jdbc.domain.product.model.ProductWithMetrics;
import jdbc.domain.product.dto.SellerSummary;
import jdbc.domain.product.service.ProductService;

import java.sql.SQLException;
import java.util.Objects;

public class ProductServiceTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== ProductService Manual Smoke Test ===");

            ProductService svc = new ProductService();

            Long sellerId = pickAnySellerId();
            if (sellerId == null) {
                System.out.println("[중단] users 테이블에 판매자(user) 데이터가 없습니다. 최소 1명 추가 후 다시 실행하세요.");
                return;
            }
            System.out.println("사용할 sellerId=" + sellerId);

            // 1) 페이징 전체
            System.out.println("\n[1] getPage(limit=5, offset=0)");
            Page<ProductWithMetrics> page = svc.getPage(5, 0);
            printPage(page);

            // 2) 생성
            System.out.println("\n[2] create(Product)");
            String name = "ServiceRunner-" + System.currentTimeMillis();
            Product p = new Product(name, 123000L, 7L, sellerId, ProductStatus.ON_SALE);

            int rows = svc.create(p);
            System.out.println("create rows=" + rows);

            Long newId = findNewestProductIdByNameAndSeller(name, sellerId);
            if (newId == null) {
                System.out.println("[중단] 방금 생성한 상품을 찾지 못했습니다.");
                return;
            }
            System.out.println("생성된 product_id=" + newId);

            // 3) 수정
            System.out.println("\n[3] update(Product)");
            p.setProductId(newId);
            p.setPrice(99000L);
            p.setStatus(ProductStatus.RESERVED);
            rows = svc.update(p);
            System.out.println("update rows=" + rows);

            // 4) 상태만 변경
            System.out.println("\n[4] updateStatus -> SOLD");
            rows = svc.updateStatus(newId, ProductStatus.SOLD);
            System.out.println("updateStatus rows=" + rows);

            // 5) 상세 + 조회수 증가(트랜잭션)
            System.out.println("\n[5] getDetailAndAddView (view +1)");
            ProductWithMetrics detail = svc.getDetailAndAddView(newId);
            printDetail(detail);

            // 6) 좋아요 등록/해제(트랜잭션)
            System.out.println("\n[6] like → like → unlike");
            detail = svc.like(newId);
            printDetail(detail);
            detail = svc.like(newId);
            printDetail(detail);
            detail = svc.unlike(newId);
            printDetail(detail);

            // 7) 검색 페이징
            System.out.println("\n[7] searchPage(nameLike=" + name + ")");
            Page<ProductWithMetrics> search = svc.searchPage(10, 0, name, null, null, null);
            printPage(search);

            // 8) 소프트 삭제
            System.out.println("\n[8] delete (soft)");
            boolean deleted = svc.delete(newId);
            System.out.println("deleted=" + deleted);

            // 9) 삭제 후 검색 (0건 기대)
            System.out.println("\n[9] 삭제 후 searchPage (0건 기대)");
            Page<ProductWithMetrics> after = svc.searchPage(10, 0, name, null, null, null);
            printPage(after);

            System.out.println("\n=== 완료 ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- helpers ----

    /** 임의의 유효한 판매자 한 명 고르기 */
    private static Long pickAnySellerId() throws SQLException {
        // deleted_at이 없거나 NULL인 사용자 중 가장 빠른 id
        return JdbcSupport.query(
                "SELECT user_id FROM users WHERE deleted_at IS NULL OR deleted_at IS NULL IS NULL ORDER BY user_id ASC LIMIT 1",
                rs -> rs.next() ? rs.getLong(1) : null
        );
    }

    /** 방금 만든 상품의 id를 (name, seller_id)로 역추적 */
    private static Long findNewestProductIdByNameAndSeller(String name, Long sellerId) throws SQLException {
        return JdbcSupport.query("""
                SELECT product_id
                FROM products
                WHERE name = ? AND seller_id = ? AND deleted_at IS NULL
                ORDER BY product_id DESC
                LIMIT 1
            """, rs -> rs.next() ? rs.getLong(1) : null, name, sellerId);
    }

    private static void printPage(Page<ProductWithMetrics> page) {
        System.out.printf("total=%d, limit=%d, offset=%d, totalPages=%d, currentPage=%d%n",
                page.total(), page.limit(), page.offset(), page.totalPages(), page.currentPage());
        for (ProductWithMetrics p : page.content()) {
            var s = p.seller();
            System.out.printf("- #%d %s | %,d원 | cat=%s | status=%s | like=%d view=%d | seller=%s(%s)%n",
                    p.productId(), p.name(), p.price(),
                    String.valueOf(p.categoryId()),
                    String.valueOf(p.status()),
                    p.likeCount(), p.viewCount(),
                    s == null ? "?" : s.sellerName(),
                    s == null ? "?" : s.sellerEmail());
        }
    }

    private static void printDetail(ProductWithMetrics p) {
        SellerSummary s = p.seller();
        System.out.printf("DETAIL: #%d %s | %,d원 | status=%s | like=%d view=%d | seller=%s(%s)%n",
                p.productId(), p.name(), p.price(), String.valueOf(p.status()),
                p.likeCount(), p.viewCount(),
                s == null ? "?" : s.sellerName(),
                s == null ? "?" : s.sellerEmail());
    }
}
