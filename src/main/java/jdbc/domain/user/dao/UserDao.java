package jdbc.domain.user.dao;

import jdbc.domain.user.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao {

    User findById(@Param("id") Long id);

    List<User> findAll();

    /** 페이징 목록 */
    List<User> findPage(@Param("limit") int limit, @Param("offset") int offset);

    /** 전체 카운트 */
    int countAll();

    int insertUser(User user);

    int deleteUser(@Param("id") Long id);
}
