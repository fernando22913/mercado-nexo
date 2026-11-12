package cl.mercadonexo.model.dao;

import cl.mercadonexo.model.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import java.util.List;

/**
 * DAO de la entidad Categoria (capa Modelo).
 * Toda operación se ejecuta con EntityManager propio y transacción controlada.
 */
public class CategoriaDAO {

    public void crear(Categoria categoria) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Categoria buscarPorId(Integer id) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.find(Categoria.class, id);
        } finally {
            em.close();
        }
    }

    public List<Categoria> listar() {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.createNamedQuery("Categoria.listar", Categoria.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Categoria buscarPorNombre(String nombre) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.createNamedQuery("Categoria.porNombre", Categoria.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Categoria actualizar(Categoria categoria) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            Categoria gestionada = em.merge(categoria);
            em.getTransaction().commit();
            return gestionada;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void eliminar(Integer id) {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            em.getTransaction().begin();
            Categoria categoria = em.find(Categoria.class, id);
            if (categoria != null) {
                em.remove(categoria);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public long contar() {
        EntityManager em = ConexionJPA.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
