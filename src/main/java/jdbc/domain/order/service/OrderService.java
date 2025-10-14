package jdbc.domain.order.service;

import jdbc.common.exception.DataAccessException;
import jdbc.common.paging.Page;
import jdbc.common.transaction.TransactionHandler;
import jdbc.domain.order.dao.OrderDao;
import jdbc.domain.order.dto.OrderSummary;
import jdbc.domain.order.model.Order;
import jdbc.domain.order.model.OrderState;
import jdbc.domain.product.dao.ProductDao;
import jdbc.domain.product.model.Product;
import jdbc.domain.product.model.ProductStatus;

public class OrderService {
    private final OrderDao orderDao = OrderDao.getInstance();
    private final ProductDao productDao = ProductDao.getInstance();

    public Page<OrderSummary> getBuyerOrders(Long buyerId, int limit, int offset, OrderState state) {
        return orderDao.findBuyerPage(buyerId, limit, offset, state);
    }

    public Page<OrderSummary> getSellerOrders(Long sellerId, int limit, int offset, OrderState state) {
        return orderDao.findSellerPage(sellerId, limit, offset, state);
    }

    public Page<OrderSummary> getAdminOrders(int limit, int offset, OrderState state, Long buyerId, Long productId) {
        return orderDao.findAdminPage(limit, offset, state, buyerId, productId);
    }

    public Long requestReservation(Long productId, Long buyerId) {
        return TransactionHandler.inTransaction(con -> {
            Product p = productDao.findById(con, productId);
            if (p == null) {
                throw new DataAccessException("Product not found. id=" + productId);
            }
            if (p.getSellerId() != null && p.getSellerId().equals(buyerId)) {
                throw new DataAccessException("Seller cannot buy own product");
            }

            // 1) 상품을 ON_SALE → RESERVED로 조건부 전이 (동시성 제어)
            int reserved = productDao.reserveIfOnSale(con, productId);
            if (reserved != 1) {
                // 이미 RESERVED/SOLD 등이라 예약 불가
                throw new DataAccessException("Product is not available for reservation");
            }

            // 2) 활성 주문 중복 체크 (선택: 상태 전이만으로도 충분하지만, 안전망으로 유지 가능)
            Order active = orderDao.findActiveByProductId(con, productId);
            if (active != null) {
                // 이 상황은 거의 없지만, 혹시 이미 존재하면 되돌리기
                productDao.setOnSaleIfReserved(con, productId);
                throw new DataAccessException("Active order already exists");
            }

            // 3) 주문 생성(REQUESTED)
            int priceAtOrder = Math.toIntExact(p.getPrice());
            return orderDao.insertRequested(con, productId, buyerId, priceAtOrder);
        });
    }

    public void accept(Long orderId, Long sellerId) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) {
                throw new DataAccessException("Order not found. id=" + orderId);
            }
            Product p = productDao.findById(con, o.getProductId());
            if (p == null) {
                throw new DataAccessException("Product not found");
            }
            if (p.getSellerId() == null || !p.getSellerId().equals(sellerId)) {
                throw new DataAccessException("No permission to accept: seller mismatch (productSeller=" + p.getSellerId() + ", caller=" + sellerId + ")");
            }

            // 주문 REQUESTED → ACCEPTED
            int updated = orderDao.updateState(con, orderId, OrderState.REQUESTED, OrderState.ACCEPTED);
            if (updated != 1) {
                throw new DataAccessException("Order state transition failed: expected REQUESTED -> ACCEPTED, current=" + o.getOrderState());
            }
            // 상품은 이미 RESERVED 상태이므로 변경 없음
            return null;
        });
    }

    public void reject(Long orderId, Long sellerId) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) {
                throw new DataAccessException("Order not found. id=" + orderId);
            }
            Product p = productDao.findById(con, o.getProductId());
            if (p == null) {
                throw new DataAccessException("Product not found");
            }
            if (p.getSellerId() == null || !p.getSellerId().equals(sellerId)) {
                throw new DataAccessException("No permission to reject: seller mismatch (productSeller=" + p.getSellerId() + ", caller=" + sellerId + ")");
            }

            // 주문 REQUESTED → REJECTED
            int updated = orderDao.updateState(con, orderId, OrderState.REQUESTED, OrderState.REJECTED);
            if (updated != 1) {
                throw new DataAccessException("Order state transition failed: expected REQUESTED -> REJECTED, current=" + o.getOrderState());
            }

            // 상품 RESERVED → ON_SALE (조건부)
            productDao.setOnSaleIfReserved(con, p.getProductId());
            return null;
        });
    }

    public void cancelByBuyer(Long orderId, Long buyerId) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) {
                throw new DataAccessException("Order not found. id=" + orderId);
            }
            if (!o.getBuyerId().equals(buyerId)) {
                throw new DataAccessException("No permission to cancel");
            }

            // 주문 REQUESTED → CANCELLED_BY_BUYER
            int rows = orderDao.updateState(con, orderId, OrderState.REQUESTED, OrderState.CANCELLED_BY_BUYER);
            if (rows != 1) {
                throw new DataAccessException("Order not cancellable");
            }

            // 상품 RESERVED → ON_SALE (조건부)
            productDao.setOnSaleIfReserved(con, o.getProductId());
            return null;
        });
    }

    public void complete(Long orderId, Long sellerId) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) {
                throw new DataAccessException("Order not found. id=" + orderId);
            }
            Product p = productDao.findById(con, o.getProductId());
            if (p == null) {
                throw new DataAccessException("Product not found");
            }
            if (p.getSellerId() == null || !p.getSellerId().equals(sellerId)) {
                throw new DataAccessException("No permission to complete: seller mismatch (productSeller=" + p.getSellerId() + ", caller=" + sellerId + ")");
            }

            // 주문 ACCEPTED → COMPLETED
            int updated = orderDao.updateState(con, orderId, OrderState.ACCEPTED, OrderState.COMPLETED);
            if (updated != 1) {
                throw new DataAccessException("Order state transition failed: expected ACCEPTED -> COMPLETED, current=" + o.getOrderState());
            }

            // 상품 RESERVED → SOLD (조건부)
            int sold = productDao.setSoldIfReserved(con, p.getProductId());
            if (sold != 1) {
                // 상태가 이미 SOLD/다른 값이면 정책에 맞춰 처리(여긴 방어적 체크)
                throw new DataAccessException("Product state inconsistent for completion");
            }
            return null;
        });
    }





    public void markReviewed(Long orderId, Long reviewerUserId) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) {
                throw new DataAccessException("Order not found. id=" + orderId);
            }
            if (o.getOrderState() != OrderState.COMPLETED) {
                throw new DataAccessException("Order not completed");
            }
            orderDao.markReviewed(con, orderId);
            return null;
        });
    }
}

