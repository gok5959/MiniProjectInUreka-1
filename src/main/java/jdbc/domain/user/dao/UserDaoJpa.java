package jdbc.domain.user.dao;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jdbc.common.jpa.JpaProvider;
import jdbc.common.transaction.TxTemplate;
import jdbc.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class UserDaoJpa implements UserDao {

    private static final UserDaoJpa INSTANCE = new UserDaoJpa();

    private final EntityManagerFactory emf;

    private UserDaoJpa() {
        this.emf = JpaProvider.emf();
    }

    public static UserDaoJpa getInstance() {
        return INSTANCE;
    }

    @Override
    public User findById(Long id) {
        return TxTemplate.readOnly(em -> {
            User u = em.find(User.class, id);
            return (u != null && u.getDeletedAt() == null) ? u : null;
        });
    }

    @Override
    public List<User> findAll() {
        return TxTemplate.readOnly(em ->
                em.createQuery("select u from User u where u.deletedAt is null order by u.id desc", User.class).getResultList());
    }

    @Override
    public List<User> findPage(int limit, int offset) {
        return TxTemplate.readOnly(em -> {
            TypedQuery<User> q = em.createQuery(
                    "select u from User u where u.deletedAt is null order by u.id desc", User.class
            );
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.getResultList();
        });
    }

    @Override
    public int countAll() {
        return TxTemplate.readOnly(em ->
                em.createQuery(
                        "select count(u) from User u where u.deletedAt is null",
                        Long.class
                ).getSingleResult().intValue()
        );
    }

    @Override
    public int insertUser(User user) {
        return TxTemplate.tx(em -> {
            em.persist(user);
            // IDENTITY 전략이면 persist 직후 insert가 발생하며 id가 세팅됩니다.
            return 1;
        });
    }

    @Override
    public int deleteUser(Long id) {
        return TxTemplate.tx(em -> {
            // soft delete (deleted_at = now)
            int updated = em.createQuery(
                            "update User u set u.deletedAt = :now where u.id = :id and u.deletedAt is null"
                    )
                    .setParameter("now", LocalDateTime.now())
                    .setParameter("id", id)
                    .executeUpdate();
            return updated;
        });
    }
}
