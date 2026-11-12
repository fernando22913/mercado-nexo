package cl.mercadonexo;

import cl.mercadonexo.model.dao.ConexionJPA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de conectividad: JPA (EclipseLink) contra PostgreSQL en Docker.
 * Requiere que el contenedor esté arriba: docker compose up -d
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConexionJPATest {

    private EntityManagerFactory emf;

    @BeforeAll
    void iniciar() {
        emf = ConexionJPA.getEntityManagerFactory();
    }

    @Test
    void fabricaDeEntidadesSeCrea() {
        assertNotNull(emf, "El EntityManagerFactory debe crearse con la BD disponible");
        assertTrue(emf.isOpen());
    }

    @Test
    void conexionConPostgresEjecutaConsulta() {
        assertDoesNotThrow(() -> {
            try (EntityManager em = emf.createEntityManager()) {
                // Consulta nativa simple para validar la conexión real
                Object resultado = em.createNativeQuery("SELECT 1").getSingleResult();
                assertNotNull(resultado);
            }
        });
    }
}
