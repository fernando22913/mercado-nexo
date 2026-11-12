package cl.mercadonexo.controller;

import cl.mercadonexo.model.Categoria;
import cl.mercadonexo.model.dao.CategoriaDAO;
import cl.mercadonexo.model.dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * CONTROLADOR de categorías.
 * Permite listar, registrar y eliminar categorías. La eliminación respeta la
 * integridad referencial: si la categoría tiene productos asociados se informa
 * el error sin dañar los datos.
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {"/categorias"})
public class CategoriaServlet extends HttpServlet {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categorias", categoriaDAO.listar());
        request.getRequestDispatcher("/WEB-INF/views/categorias/lista.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = ProductoServlet.param(request, "action", "crear");
        if ("eliminar".equals(accion)) {
            eliminar(request, response);
        } else {
            crear(request, response);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String nombre = ProductoServlet.trim(request.getParameter("nombre"));
        String descripcion = ProductoServlet.trim(request.getParameter("descripcion"));

        List<String> errores = new java.util.ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            errores.add("El nombre de la categoría es obligatorio.");
        } else if (nombre.length() > 60) {
            errores.add("El nombre no puede exceder 60 caracteres.");
        }
        if (descripcion != null && descripcion.length() > 200) {
            errores.add("La descripción no puede exceder 200 caracteres.");
        }
        if (errores.isEmpty() && categoriaDAO.buscarPorNombre(nombre) != null) {
            errores.add("Ya existe una categoría con ese nombre.");
        }

        if (!errores.isEmpty()) {
            request.setAttribute("errores", errores);
            request.setAttribute("nombreIngresado", nombre);
            request.setAttribute("descripcionIngresada", descripcion);
            request.setAttribute("categorias", categoriaDAO.listar());
            request.getRequestDispatcher("/WEB-INF/views/categorias/lista.jsp").forward(request, response);
            return;
        }

        categoriaDAO.crear(new Categoria(nombre, descripcion));
        request.getSession().setAttribute("mensajeExito",
                "Categoría \"" + nombre + "\" registrada correctamente.");
        response.sendRedirect(ProductoServlet.ruta(request));
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = ProductoServlet.enteroONullOr(request.getParameter("id"));
        if (id != null) {
            long productosAsociados = productoDAO.filtrar(null, id).size();
            if (productosAsociados > 0) {
                request.getSession().setAttribute("mensajeError",
                        "No se puede eliminar: la categoría tiene " + productosAsociados
                        + " producto(s) asociado(s).");
            } else {
                categoriaDAO.eliminar(id);
                request.getSession().setAttribute("mensajeExito", "Categoría eliminada correctamente.");
            }
        } else {
            request.getSession().setAttribute("mensajeError", "Categoría no válida.");
        }
        response.sendRedirect(ProductoServlet.ruta(request));
    }
}
