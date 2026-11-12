package cl.mercadonexo.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Administrador de la unidad de persistencia.
 * Crea un único {@link EntityManagerFactory} para la unidad "mercadonexoPU"
 * (definida en META-INF/persistence.xml) y entrega {@link EntityManager}
 * a la capa DAO. Cada DAO abre/cierra su propio EntityManager y controla
 * sus transacciones con {@link jakarta.persistence.EntityTransaction}.
 */
public final class ConexionJPA {

    private static final String UNIDAD_PERSISTENCIA = "mercadonexoPU";
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory(UNIDAD_PERSISTENCIA);

    private ConexionJPA() {
    }

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }
}
