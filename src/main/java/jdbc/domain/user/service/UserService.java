package jdbc.domain.user.service;

import jdbc.common.config.MybatisConfig;
import jdbc.common.exception.DataAccessException;
import jdbc.common.paging.Page;
import jdbc.domain.user.dao.UserDao;
import jdbc.domain.user.dao.UserDaoJpa;
import jdbc.domain.user.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    UserDao userDao;

    public UserService(UserDao userDao) throws IOException {
        this.userDao = UserDaoJpa.getInstance();
    }

    public User getById(Long id) {
        try {
            return userDao.findById(id);
        } catch (RuntimeException e) {
            throw new DataAccessException("getById failed : id=" + id, e);
        }
    }

    public Page<User> getAllInPage(int limit, int offset) {
        try {
            int total = userDao.countAll();
            List<User> content = userDao.findPage(limit, offset);
            return new Page<>(content, total, limit, offset);
        } catch (RuntimeException e) {
            throw new DataAccessException("getPage failed : limit=" + limit + ", offset=" + offset, e);
        }
    }

    public int create(User user) {
        try {
            return userDao.insertUser(user);
        } catch (RuntimeException e) {
            throw new DataAccessException("createUser failed : user=" + user, e);
        }
    }

    public int delete(Long id) {
        try {
            return userDao.deleteUser(id);
        } catch (RuntimeException e) {
            throw new DataAccessException("delete failed : id=" + id, e);
        }
    }
}
