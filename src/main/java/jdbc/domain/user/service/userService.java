package jdbc.domain.user.service;

import jdbc.common.exception.DataAccessException;
import jdbc.common.paging.Page;
import jdbc.domain.user.dao.UserDao;
import jdbc.domain.user.model.User;

import java.sql.SQLException;

public class userService {
    UserDao userDao;

    public userService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserById(Long id) {
        try {
            return userDao.findById(id);
        } catch (SQLException e) {
            throw new DataAccessException("getById failed : id=" +id, e);
        }
    }

    public Page<User> getUserInPage(int limit, int offset) {
        try {
            return userDao.findInPage(limit, offset);
        } catch(SQLException e) {
            throw new DataAccessException("getPage failed : limit=" + limit + ", offset=" + offset, e);
        }
    }

    public int createUser(User user) {
        try {
            return userDao.insertUser(user);
        } catch(SQLException e) {
            throw new DataAccessException("createUser failed : user=" + user, e);
        }
    }

    public int delete(Long id) {
        try {
            return userDao.deleteUser(id);
        } catch(SQLException e) {
            throw new DataAccessException("delete failed : id=" +id, e);
        }
    }
}
