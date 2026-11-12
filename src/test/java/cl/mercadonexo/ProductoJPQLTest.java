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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tarea 6 — Pruebas de las consultas JPQL:
 *   1) consulta general      -> Producto.listar
 *   2) consulta parametrizada-> Producto.filtrar (nombre y/o categoría)
 *   3) UPDATE masivo         -> actualizarPreciosPorCategoria
 *   4) DELETE masivo         -> eliminarPorCategoria / eliminarAgotados
 * Requiere: docker compose up -d
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductoJPQLTest {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private Categoria categoriaA; // categoría de prueba (recep. del bulk)
    private Categoria categoriaB; // categoría de control (no debe modificarse)
    private final String sufijo = String.valueOf(System.currentTimeMillis() % 100000);

    @BeforeAll
    void semilla() {
        categoriaA = new Categoria("TestA " + sufijo, "para operaciones masivas");
        categoriaB = new Categoria("TestB " + sufijo, "grupo de control");
        categoriaDAO.crear(categoriaA);
        categoriaDAO.crear(categoriaB);

        productoDAO.crear(new Producto("ProdA1 " + sufijo, categoriaA, new BigDecimal("100.00"), 5));
        productoDAO.crear(new Producto("ProdA2 " + sufijo, categoriaA, new BigDecimal("200.00"), 0));
        productoDAO.crear(new Producto("ProdA3 " + sufijo, categoriaA, new BigDecimal("300.00"), 8));
        productoDAO.crear(new Producto("ProdB1 " + sufijo, categoriaB, new BigDecimal("100.00"), 3));
    }

    @AfterAll
    void limpieza() {
        // DELETE masivo de las categorías de prueba (también valida el bulk delete)
        productoDAO.eliminarPorCategoria(categoriaA.getId());
        productoDAO.eliminarPorCategoria(categoriaB.getId());
        categoriaDAO.eliminar(categoriaA.getId());
        categoriaDAO.eliminar(categoriaB.getId());
    }

    @Test
    @Order(1)
    void consultaGeneralListaTodos() {
        List<Producto> todos = productoDAO.listar();
        assertTrue(todos.size() >= 4);
        // Ordenada por id
        for (int i = 1; i < todos.size(); i++) {
            assertTrue(todos.get(i - 1).getId() <= todos.get(i).getId());
        }
    }

    @Test
    @Order(2)
    void consultaParametrizadaPorNombre() {
        List<Producto> resultado = productoDAO.filtrar("ProdA1 " + sufijo, null);
        assertEquals(1, resultado.size());
        assertEquals("ProdA1 " + sufijo, resultado.get(0).getNombre());
    }

    @Test
    @Order(3)
    void consultaParametrizadaPorCategoria() {
        List<Producto> resultado = productoDAO.filtrar(null, categoriaA.getId());
        assertEquals(3, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getCategoria().getId().equals(categoriaA.getId())));
    }

    @Test
    @Order(4)
    void consultaParametrizadaCombinada() {
        List<Producto> resultado = productoDAO.filtrar("ProdB", categoriaB.getId());
        assertEquals(1, resultado.size());
        List<Producto> vacia = productoDAO.filtrar("inexistente-xyz", categoriaA.getId());
        assertTrue(vacia.isEmpty());
    }

    @Test
    @Order(5)
    void updateMasivoAjustaPreciosDeUnaCategoria() {
        // +10% a la categoría A: 100->110, 200->220, 300->330
        int afectados = productoDAO.actualizarPreciosPorCategoria(categoriaA.getId(), 10.0);
        assertEquals(3, afectados);

        // Verificación con una lectura fresca desde la BD
        List<Producto> categoriaATras = productoDAO.filtrar(null, categoriaA.getId());
        assertEquals(new BigDecimal("110.00"), categoriaATras.get(0).getPrecio());
        assertEquals(new BigDecimal("220.00"), categoriaATras.get(1).getPrecio());
        assertEquals(new BigDecimal("330.00"), categoriaATras.get(2).getPrecio());

        // La categoría B (control) no cambió
        Producto control = productoDAO.filtrar("ProdB1 " + sufijo, null).get(0);
        assertEquals(new BigDecimal("100.00"), control.getPrecio());
    }

    @Test
    @Order(6)
    void deleteMasivoPorCategoria() {
        int afectados = productoDAO.eliminarPorCategoria(categoriaB.getId());
        assertEquals(1, afectados);
        assertTrue(productoDAO.filtrar("ProdB1 " + sufijo, null).isEmpty());
    }

    @Test
    @Order(7)
    void deleteMasivoDeAgotados() {
        // Tras el +10%, en A quedan ProdA1 (110) y ProdA3 (330) con stock; ProdA2 tiene stock 0
        int afectados = productoDAO.eliminarAgotados();
        assertTrue(afectados >= 1);
        assertTrue(productoDAO.filtrar("ProdA2 " + sufijo, null).isEmpty(),
                "El producto agotado debe haber sido eliminado por el DELETE masivo");
    }
}
