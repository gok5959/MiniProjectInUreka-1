package jdbc.common.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaProvider {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("my-pu");

    private JpaProvider(){}

    public static EntityManagerFactory emf() {
        return emf;
    }

    public static void shutdown() {
        if (emf.isOpen()) emf.close();
    }
}
