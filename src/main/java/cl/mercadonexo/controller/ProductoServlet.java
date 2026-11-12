package cl.mercadonexo.controller;

import cl.mercadonexo.model.Categoria;
import cl.mercadonexo.model.Producto;
import cl.mercadonexo.model.dao.CategoriaDAO;
import cl.mercadonexo.model.dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * CONTROLADOR del catálogo de productos.
 *
 * Recibe las solicitudes HTTP, valida la entrada, invoca la capa Modelo
 * (DAO con EntityManager) y decide la vista JSP que se presentará.
 * Ninguna vista ejecuta persistencia directamente: solo reciben objetos
 * ya preparados por este controlador.
 */
@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos"})
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = param(request, "action", "listar");
        switch (accion) {
            case "nuevo"      -> prepararFormulario(request, null);
            case "editar"     -> prepararEdicion(request);
            case "detalle"    -> mostrarDetalle(request);
            case "confirmar"  -> mostrarConfirmacion(request);
            default           -> listarProductos(request);
        }
        // Toda acción termina enviando la vista JSP correspondiente
        request.getRequestDispatcher(vista(request)).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = param(request, "action", "");
        switch (accion) {
            case "guardar"          -> guardar(request, response);
            case "eliminar"         -> eliminar(request, response);
            case "aumentoPrecios"   -> aumentoPrecios(request, response);
            case "eliminarCategoria"-> eliminarPorCategoria(request, response);
            case "eliminarAgotados" -> eliminarAgotados(request, response);
            default                 -> response.sendRedirect(ruta(request) + "?action=listar");
        }
    }

    // ============================ Acciones ============================

    /** Listado con filtros (JPQL parametrizada) + datos para acciones masivas. */
    private void listarProductos(HttpServletRequest request) {
        String nombre = trim(request.getParameter("nombre"));
        Integer categoriaId = enteroONullOr(request.getParameter("categoriaId"));

        List<Producto> productos = productoDAO.filtrar(nombre, categoriaId);
        request.setAttribute("productos", productos);
        request.setAttribute("categorias", categoriaDAO.listar());
        request.setAttribute("filtroNombre", nombre);
        request.setAttribute("filtroCategoriaId", categoriaId);
        request.setAttribute("total", productos.size());
        request.setAttribute("vista", "lista");
    }

    /** Formulario vacío para crear un producto. */
    private void prepararFormulario(HttpServletRequest request, Producto producto) {
        if (producto == null) {
            producto = new Producto();
        }
        request.setAttribute("producto", producto);
        request.setAttribute("categorias", categoriaDAO.listar());
        request.setAttribute("vista", "formulario");
    }

    /** Formulario precargado para editar un producto existente. */
    private void prepararEdicion(HttpServletRequest request) {
        Integer id = enteroONullOr(request.getParameter("id"));
        Producto producto = (id == null) ? null : productoDAO.buscarPorId(id);
        if (producto == null) {
            errorNoEncontrado(request);
            return;
        }
        prepararFormulario(request, producto);
    }

    /** Detalle de un producto. */
    private void mostrarDetalle(HttpServletRequest request) {
        Integer id = enteroONullOr(request.getParameter("id"));
        Producto producto = (id == null) ? null : productoDAO.buscarPorId(id);
        if (producto == null) {
            errorNoEncontrado(request);
            return;
        }
        request.setAttribute("producto", producto);
        request.setAttribute("vista", "detalle");
    }

    /** Página de confirmación antes de eliminar. */
    private void mostrarConfirmacion(HttpServletRequest request) {
        Integer id = enteroONullOr(request.getParameter("id"));
        Producto producto = (id == null) ? null : productoDAO.buscarPorId(id);
        if (producto == null) {
            errorNoEncontrado(request);
            return;
        }
        request.setAttribute("producto", producto);
        request.setAttribute("vista", "confirmar-eliminar");
    }

    /** Guarda (crear o actualizar según venga el id) con validación de entrada. */
    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Integer id = enteroONullOr(request.getParameter("id"));
        String nombre = trim(request.getParameter("nombre"));
        Integer categoriaId = enteroONullOr(request.getParameter("categoriaId"));
        String precioTexto = trim(request.getParameter("precio"));
        String stockTexto = trim(request.getParameter("stock"));

        // ---- Validación de entrada (responsabilidad del controlador) ----
        List<String> errores = new java.util.ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            errores.add("El nombre es obligatorio.");
        } else if (nombre.length() > 100) {
            errores.add("El nombre no puede exceder 100 caracteres.");
        }
        if (categoriaId == null) {
            errores.add("Debe seleccionar una categoría.");
        }
        BigDecimal precio = null;
        if (precioTexto == null || precioTexto.isBlank()) {
            errores.add("El precio es obligatorio.");
        } else {
            try {
                precio = new BigDecimal(precioTexto.replace(',', '.'));
                if (precio.scale() > 2) {
                    precio = precio.setScale(2, java.math.RoundingMode.DOWN);
                }
                if (precio.compareTo(BigDecimal.ZERO) < 0) {
                    errores.add("El precio no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El precio debe ser un número válido (ej: 19990 o 19990,50).");
            }
        }
        Integer stock = null;
        if (stockTexto == null || stockTexto.isBlank()) {
            errores.add("El stock es obligatorio.");
        } else {
            try {
                stock = Integer.valueOf(stockTexto);
                if (stock < 0) {
                    errores.add("El stock no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El stock debe ser un número entero.");
            }
        }

        if (!errores.isEmpty()) {            // Reenvía al formulario conservando lo ingresado y mostrando los errores
            Producto producto = new Producto();
            producto.setId(id);
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setStock(stock);
            if (categoriaId != null) {
                producto.setCategoria(categoriaDAO.buscarPorId(categoriaId));
            }
            request.setAttribute("errores", errores);
            request.getSession().setAttribute("mensajeError", "Revise los datos del formulario.");
            prepararFormulario(request, producto);
            request.getRequestDispatcher("/WEB-INF/views/productos/formulario.jsp")
                   .forward(request, response);
            return;
        }

        // ---- Persistencia a través del Modelo ----
        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);
        if (categoria == null) {
            request.getSession().setAttribute("mensajeError", "La categoría seleccionada no existe.");
            response.sendRedirect(ruta(request) + "?action=listar");
            return;
        }
        boolean esNuevo = (id == null);
        Producto producto = esNuevo ? new Producto() : productoDAO.buscarPorId(id);
        if (!esNuevo && producto == null) {
            request.getSession().setAttribute("mensajeError", "El producto solicitado no existe.");
            response.sendRedirect(ruta(request) + "?action=listar");
            return;
        }
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setStock(stock);

        if (esNuevo) {
            productoDAO.crear(producto);
            request.getSession().setAttribute("mensajeExito",
                    "Producto \"" + producto.getNombre() + "\" registrado correctamente.");
        } else {
            productoDAO.actualizar(producto);
            request.getSession().setAttribute("mensajeExito",
                    "Producto \"" + producto.getNombre() + "\" actualizado correctamente.");
        }
        response.sendRedirect(ruta(request) + "?action=detalle&id=" + producto.getId());
    }

    /** Elimina un producto individual. */
    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = enteroONullOr(request.getParameter("id"));
        if (id != null && productoDAO.eliminar(id)) {
            request.getSession().setAttribute("mensajeExito", "Producto eliminado correctamente.");
        } else {
            request.getSession().setAttribute("mensajeError", "El producto solicitado no existe.");
        }
        response.sendRedirect(ruta(request) + "?action=listar");
    }

    /** UPDATE masivo por JPQL: ajuste de precios por categoría. */
    private void aumentoPrecios(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer categoriaId = enteroONullOr(request.getParameter("masivaCategoriaId"));
        Double porcentaje = decimalONullOr(request.getParameter("porcentaje"));
        if (porcentaje == null || porcentaje < -100 || porcentaje > 100) {
            request.getSession().setAttribute("mensajeError",
                    "Indique un porcentaje entre -100 y 100 para el ajuste masivo.");
            response.sendRedirect(ruta(request) + "?action=listar");
            return;
        }
        int afectados = productoDAO.actualizarPreciosPorCategoria(categoriaId, porcentaje);
        String alcance = (categoriaId == null) ? "todas las categorías"
                : "la categoría \"" + categoriaDAO.buscarPorId(categoriaId).getNombre() + "\"";
        request.getSession().setAttribute("mensajeExito",
                "Ajuste masivo aplicado (" + porcentaje + "%) a " + afectados + " producto(s) de " + alcance + ".");
        response.sendRedirect(ruta(request) + "?action=listar");
    }

    /** DELETE masivo por JPQL: elimina los productos de una categoría. */
    private void eliminarPorCategoria(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer categoriaId = enteroONullOr(request.getParameter("masivaCategoriaId"));
        if (categoriaId == null) {
            request.getSession().setAttribute("mensajeError",
                    "Seleccione la categoría cuyos productos desea eliminar.");
            response.sendRedirect(ruta(request) + "?action=listar");
            return;
        }
        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);
        int afectados = productoDAO.eliminarPorCategoria(categoriaId);
        request.getSession().setAttribute("mensajeExito",
                "DELETE masivo: " + afectados + " producto(s) de \"" + categoria.getNombre() + "\" eliminados.");
        response.sendRedirect(ruta(request) + "?action=listar");
    }

    /** DELETE masivo por JPQL: elimina los productos sin stock. */
    private void eliminarAgotados(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int afectados = productoDAO.eliminarAgotados();
        request.getSession().setAttribute("mensajeExito",
                "DELETE masivo: " + afectados + " producto(s) sin stock eliminados.");
        response.sendRedirect(ruta(request) + "?action=listar");
    }

    // ============================ Utilidades ============================

    private void errorNoEncontrado(HttpServletRequest request) {
        request.setAttribute("vista", "no-encontrado");
        request.setAttribute("producto", null);
        request.setAttribute("categorias", categoriaDAO.listar());
    }

    private String vista(HttpServletRequest request) {
        String vista = (String) request.getAttribute("vista");
        if (vista == null) {
            vista = "lista";
        }
        if ("no-encontrado".equals(vista)) {
            return "/WEB-INF/views/productos/no-encontrado.jsp";
        }
        if ("formulario".equals(vista)) {
            return "/WEB-INF/views/productos/formulario.jsp";
        }
        if ("detalle".equals(vista)) {
            return "/WEB-INF/views/productos/detalle.jsp";
        }
        if ("confirmar-eliminar".equals(vista)) {
            return "/WEB-INF/views/productos/confirmar-eliminar.jsp";
        }
        return "/WEB-INF/views/productos/lista.jsp";
    }

    static String ruta(HttpServletRequest request) {
        return request.getContextPath() + request.getServletPath();
    }

    static String trim(String valor) {
        return valor == null ? null : valor.trim();
    }

    static Integer enteroONullOr(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static Double decimalONullOr(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : Double.valueOf(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String param(HttpServletRequest request, String nombre, String porDefecto) {
        String valor = request.getParameter(nombre);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }
}
