package cl.mercadonexo.controller;

import cl.mercadonexo.model.dao.CategoriaDAO;
import cl.mercadonexo.model.dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador de la página de inicio (solo lectura).
 *
 * No altera datos ni agrega lógica de negocio: únicamente recupera las
 * listas existentes de productos y categorías a través de los métodos
 * de consulta ya implementados en la capa Modelo (DAO), y las entrega
 * a la vista del panel de inicio para calcular las estadísticas mostradas.
 */
@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio"})
public class InicioServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Solo lectura: se reutilizan los métodos de consulta existentes.
        request.setAttribute("productos", productoDAO.listar());
        request.setAttribute("categorias", categoriaDAO.listar());
        request.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(request, response);
    }
}
