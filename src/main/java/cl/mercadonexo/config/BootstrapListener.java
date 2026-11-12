package cl.mercadonexo.config;

import cl.mercadonexo.model.Categoria;
import cl.mercadonexo.model.dao.CategoriaDAO;
import cl.mercadonexo.model.dao.ConexionJPA;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicialización de la aplicación (Tarea 2 y 4).
 *
 * Al desplegar la aplicación:
 *  1. Crea el EntityManagerFactory (valida conectividad con PostgreSQL y
 *     genera el esquema según eclipselink.ddl-generation).
 *  2. Inserta las categorías base si la tabla está vacía (datos semilla).
 */
@WebListener
public class BootstrapListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(BootstrapListener.class.getName());

    private static final String[][] CATEGORIAS_SEMILLA = {
        {"Periféricos", "Teclados, mouse, monitores y accesorios de escritorio"},
        {"Notebooks", "Equipos portátiles de trabajo y hogar"},
        {"Smartphones", "Teléfonos móviles y smartphones"},
        {"Accesorios", "Cables, fundas, cargadores y otros accesorios"}
    };

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            EntityManagerFactory emf = ConexionJPA.getEntityManagerFactory();
            LOG.info("[MercadoNexo] Unidad de persistencia iniciada: " + emf.getProperties().keySet().size()
                    + " propiedades cargadas.");

            CategoriaDAO categoriaDAO = new CategoriaDAO();
            int creadas = 0;
            for (String[] fila : CATEGORIAS_SEMILLA) {
                // Siembra idempotente: solo crea la categoría si no existe por nombre
                if (categoriaDAO.buscarPorNombre(fila[0]) == null) {
                    categoriaDAO.crear(new Categoria(fila[0], fila[1]));
                    creadas++;
                }
            }
            LOG.info("[MercadoNexo] Categorías semilla insertadas: " + creadas
                    + " · total en BD: " + categoriaDAO.contar());
            sce.getServletContext().setAttribute("appIniciada", Boolean.TRUE);
        } catch (RuntimeException e) {
            // No bloquea el despliegue: la aplicación queda arriba y muestra
            // el error si se accede a la BD sin estar disponible.
            LOG.log(Level.SEVERE, "[MercadoNexo] No se pudo inicializar la persistencia "
                    + "(¿PostgreSQL en ejecución?): " + e.getMessage(), e);
        }
    }
}
