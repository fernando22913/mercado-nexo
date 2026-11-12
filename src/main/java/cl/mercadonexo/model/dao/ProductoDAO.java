package cl.mercadonexo.model.dao;

import cl.mercadonexo.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * DAO de la entidad Producto (capa Modelo).
 *
 * Implementa:
 *  - Tarea 5: CRUD completo con EntityManager + transacciones controladas.
 *  - Tarea 6: JPQL (consulta general, parametrizada, UPDATE masivo, DELETE masivo).
 *
 * Cada operación usa un EntityManager propio; ante un error se hace rollback
 * y el contexto de persistencia se cierra, garantizando sincronización con la BD.
 */
public class ProductoDAO {

    // ==================== TAREA 5: CRUD con EntityManager ====================

    /** Registrar un producto. */
    public Producto crear(Producto producto) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(producto);
            em.getTransaction().commit();
            return producto;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Buscar un producto por clave primaria (em.find). */
    public Producto buscarPorId(Integer id) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.find(Producto.class, id);
        } finally {
            em.close();
        }
    }

    // ==================== TAREA 6.1: consulta JPQL general ====================

    /**
     * JPQL (consulta general): SELECT p FROM Producto p ORDER BY p.id
     * Recupera todos los productos del catálogo, ordenados por identificador.
     */
    public List<Producto> listar() {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.createNamedQuery("Producto.listar", Producto.class).getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== TAREA 6.2: consulta JPQL parametrizada ====================

    /**
     * JPQL (consulta con condición/parámetros): filtra por nombre (búsqueda parcial,
     * insensible a mayúsculas) y/o categoría, combinándolos con AND.
     * La consulta se arma dinámicamente según los filtros recibidos y todos los
     * valores se pasan por setParameter (nunca concatenados al string JPQL).
     */
    public List<Producto> filtrar(String nombre, Integer categoriaId) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM Producto p WHERE 1 = 1");
            boolean filtrarNombre = nombre != null && !nombre.isBlank();
            if (filtrarNombre) {
                jpql.append(" AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))");
            }
            if (categoriaId != null) {
                jpql.append(" AND p.categoria.id = :categoriaId");
            }
            jpql.append(" ORDER BY p.id");
            var query = em.createQuery(jpql.toString(), Producto.class);
            if (filtrarNombre) {
                query.setParameter("nombre", nombre.trim());
            }
            if (categoriaId != null) {
                query.setParameter("categoriaId", categoriaId);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /** Actualizar un producto (merge gestiona la entidad y sincroniza el cambio con la BD). */
    public Producto actualizar(Producto producto) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            Producto gestionado = em.merge(producto);
            em.getTransaction().commit();
            return gestionado;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Eliminar un producto por id (transacción controlada). */
    public boolean eliminar(Integer id) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            Producto producto = em.find(Producto.class, id);
            if (producto == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(producto);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // ==================== TAREA 6.3: JPQL UPDATE masivo ====================

    /**
     * JPQL (UPDATE masivo): ajusta el precio de todos los productos de una categoría.
     * UPDATE Producto p SET p.precio = p.precio * (1 + :porcentaje / 100)
     * WHERE p.categoria.id = :categoriaId
     *
     * Es una operación bulk: no pasa por el contexto de persistencia, por eso
     * se ejecuta en una transacción propia y con em.clear() al terminar, de modo
     * que futuros EntityManager lean los valores nuevos desde la base de datos.
     *
     * @param categoriaId id de la categoría (null = todas las categorías)
     * @param porcentaje  variación porcentual (positivo = aumento, negativo = rebaja)
     * @return cantidad de productos modificados
     */
    public int actualizarPreciosPorCategoria(Integer categoriaId, double porcentaje) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            // Se construye la condición según el alcance: categoría específica o todas.
            // (Se evita :categoriaId IS NULL porque PostgreSQL no puede inferir
            //  el tipo de un parámetro nulo en sentencias parametrizadas.)
            String jpql;
            if (categoriaId == null) {
                jpql = "UPDATE Producto p SET p.precio = p.precio * (1 + :porcentaje / 100)";
            } else {
                jpql = "UPDATE Producto p SET p.precio = p.precio * (1 + :porcentaje / 100) "
                     + "WHERE p.categoria.id = :categoriaId";
            }
            jakarta.persistence.Query q = em.createQuery(jpql);
            q.setParameter("porcentaje", porcentaje);
            if (categoriaId != null) {
                q.setParameter("categoriaId", categoriaId);
            }
            int afectados = q.executeUpdate();
            em.getTransaction().commit();
            // Sincroniza el contexto de persistencia con el resultado del bulk update
            em.clear();
            return afectados;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // ==================== TAREA 6.4: JPQL DELETE masivo ====================

    /**
     * JPQL (DELETE masivo): elimina todos los productos de una categoría.
     * DELETE FROM Producto p WHERE p.categoria.id = :categoriaId
     * Operación bulk: transacción propia + em.clear() para sincronización.
     */
    public int eliminarPorCategoria(Integer categoriaId) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            String jpql = "DELETE FROM Producto p WHERE p.categoria.id = :categoriaId";
            int afectados = em.createQuery(jpql)
                    .setParameter("categoriaId", categoriaId)
                    .executeUpdate();
            em.getTransaction().commit();
            em.clear();
            return afectados;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * JPQL (DELETE masivo adicional): elimina los productos sin stock (stock = 0).
     * DELETE FROM Producto p WHERE p.stock = 0
     */
    public int eliminarAgotados() {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            String jpql = "DELETE FROM Producto p WHERE p.stock = 0";
            int afectados = em.createQuery(jpql).executeUpdate();
            em.getTransaction().commit();
            em.clear();
            return afectados;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
