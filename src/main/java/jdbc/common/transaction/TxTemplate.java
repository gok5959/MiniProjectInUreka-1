package jdbc.common.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jdbc.common.jpa.JpaProvider;
import jdbc.common.transaction.JpaWork;

public class TxTemplate {
    private TxTemplate() {}

    public static <T> T tx(JpaWork<T> work) {
        EntityManager em = JpaProvider.emf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.execute(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // 읽기 전용(트랜잭션 없이)도 필요하면 제공
    public static <T> T readOnly(JpaWork<T> work) {
        EntityManager em = JpaProvider.emf().createEntityManager();
        try {
            return work.execute(em);
        } finally {
            em.close();
        }
    }
}
