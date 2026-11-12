package cl.mercadonexo;

import cl.mercadonexo.model.Categoria;
import cl.mercadonexo.model.Producto;
import cl.mercadonexo.model.dao.CategoriaDAO;
import cl.mercadonexo.model.dao.ProductoDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tarea 5 — Pruebas del CRUD con EntityManager.
 * Cada operación se verifica re-leyendo desde la base de datos
 * (nuevo EntityManager), demostrando que los cambios persisten.
 * Requiere: docker compose up -d
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoCRUDTest {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private Integer categoriaId;
    private Integer productoId;
    private final String sufijo = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    void semilla() {
        // La relación requiere una categoría real en la BD
        Categoria categoria = categoriaDAO.buscarPorNombre("Periféricos");
        if (categoria == null) {
            categoria = new Categoria("Periféricos", "Semilla de prueba");
            categoriaDAO.crear(categoria);
        }
        categoriaId = categoria.getId();
    }

    @AfterAll
    void limpieza() {
        // Deja la base limpia de los datos generados por esta suite
        productoDAO.filtrar(null, null).stream()
                .filter(p -> p.getNombre().contains("Test"))
                .forEach(p -> productoDAO.eliminar(p.getId()));
    }

    @Test
    @Order(1)
    void registrarProducto() {
        Producto producto = new Producto("Teclado Test " + sufijo,
                categoriaDAO.buscarPorId(categoriaId),
                new BigDecimal("24990.50"), 15);
        Producto guardado = productoDAO.crear(producto);
        assertNotNull(guardado.getId(), "El id debe generarse (IDENTITY) al persistir");
        productoId = guardado.getId();
    }

    @Test
    @Order(2)
    void buscarProductoRegistrado() {
        Producto leido = productoDAO.buscarPorId(productoId);
        assertNotNull(leido);
        assertEquals("Teclado Test " + sufijo, leido.getNombre());
        // La relación se lee correctamente (ManyToOne -> Categoria)
        assertEquals("Periféricos", leido.getCategoria().getNombre());
        assertEquals(new BigDecimal("24990.50"), leido.getPrecio());
        assertEquals(15, leido.getStock());
    }

    @Test
    @Order(3)
    void actualizarProducto() {
        Producto producto = productoDAO.buscarPorId(productoId);
        producto.setNombre("Mouse Test " + sufijo);
        producto.setPrecio(new BigDecimal("18990"));
        producto.setStock(40);
        productoDAO.actualizar(producto);

        // Verificación con un EntityManager nuevo: el cambio quedó en la BD
        Producto releyendo = productoDAO.buscarPorId(productoId);
        assertEquals("Mouse Test " + sufijo, releyendo.getNombre());
        assertEquals(new BigDecimal("18990"), releyendo.getPrecio());
        assertEquals(40, releyendo.getStock());
    }

    @Test
    @Order(4)
    void listarProductosContieneElActualizado() {
        List<Producto> todos = productoDAO.listar();
        assertFalse(todos.isEmpty());
        assertTrue(todos.stream().anyMatch(p -> p.getId().equals(productoId)));
    }

    @Test
    @Order(5)
    void eliminarProducto() {
        assertTrue(productoDAO.eliminar(productoId));
        assertNull(productoDAO.buscarPorId(productoId), "Tras eliminar no debe existir en la BD");
    }
}
