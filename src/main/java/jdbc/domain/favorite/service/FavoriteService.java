package jdbc.domain.favorite.service;

import jdbc.common.exception.DataAccessException;
import jdbc.common.transaction.TransactionHandler;
import jdbc.domain.favorite.dao.FavoriteDao;
import jdbc.domain.favorite.dto.FavoriteDto;
import java.util.List;

public class FavoriteService {

    private final FavoriteDao favoriteDao;

    public FavoriteService() {
        this.favoriteDao = FavoriteDao.getInstance();
    }

    public boolean toggleFavorite(Long userId, Long productId) {
        return TransactionHandler.inTransaction(con -> {
            try {
                var existing = favoriteDao.find(con, userId, productId);
                if (existing == null) {
                    // insert active
                    favoriteDao.insert(con, userId, productId);
                    return true;
                } else {
                    boolean newActive = !existing.active();
                    favoriteDao.updateActive(con, userId, productId, newActive);
                    return newActive;
                }
            } catch (Exception e) {
                throw new DataAccessException("toggleFavorite failed", e);
            }
        });
    }

    public List<FavoriteDto> findByUser(Long userId, int limit, int offset) {
        return favoriteDao.findByUser(userId, limit, offset);
    }
}
