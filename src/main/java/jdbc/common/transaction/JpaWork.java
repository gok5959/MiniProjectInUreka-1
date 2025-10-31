package jdbc.common.transaction;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface JpaWork<T> {
    T execute(EntityManager em);
}
