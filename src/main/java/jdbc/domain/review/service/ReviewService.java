package jdbc.domain.review.service;

import jdbc.common.exception.DataAccessException;
import jdbc.common.paging.Page;
import jdbc.common.transaction.TransactionHandler;
import jdbc.domain.order.dao.OrderDao;
import jdbc.domain.order.model.Order;
import jdbc.domain.order.model.ReviewStatus;
import jdbc.domain.review.dao.ReviewDao;
import jdbc.domain.review.dto.ReviewDto;

public class ReviewService {
    private final ReviewDao reviewDao = ReviewDao.getInstance();
    private final OrderDao orderDao = OrderDao.getInstance();

    public Page<ReviewDto> findByProductId(Long productId, int limit, int offset) {
        return reviewDao.findByProductId(productId, limit, offset);
    }

    public void createReview(Long orderId, Long reviewerId, String title, String content, java.math.BigDecimal rating) {
        TransactionHandler.inTransaction(con -> {
            Order o = orderDao.findById(con, orderId);
            if (o == null) throw new DataAccessException("Order not found. id=" + orderId);
            if (!o.getBuyerId().equals(reviewerId)) throw new DataAccessException("No permission to review");
            if (o.getReviewStatus() == null || o.getReviewStatus() != ReviewStatus.BEFORE_REVIEW) throw new DataAccessException("Order is not in before-review status");
            // Insert review
            int inserted = reviewDao.insert(orderId, reviewerId, title, content, rating);
            if (inserted < 1) throw new DataAccessException("Insert review failed");
            // mark order reviewed
            orderDao.markReviewed(con, orderId);
            return null;
        });
    }
}
