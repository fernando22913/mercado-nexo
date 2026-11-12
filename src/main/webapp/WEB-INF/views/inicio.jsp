<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Panel de inicio"/>
<c:set var="seccionActual" value="inicio"/>

<%-- ---------- Estadísticas calculadas en la vista (solo lectura) ---------- --%>
<c:set var="totalProductos" value="0"/>
<c:set var="agotados" value="0"/>
<c:set var="valorInventario" value="0"/>
<c:forEach var="p" items="${productos}">
    <c:set var="totalProductos" value="${totalProductos + 1}"/>
    <c:if test="${p.stock == 0}">
        <c:set var="agotados" value="${agotados + 1}"/>
    </c:if>
    <c:set var="valorInventario" value="${valorInventario + p.precio * p.stock}"/>
</c:forEach>
<c:set var="totalCategorias" value="0"/>
<c:forEach var="cat" items="${categorias}">
    <c:set var="totalCategorias" value="${totalCategorias + 1}"/>
</c:forEach>

<%@ include file="includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>Bienvenido a MercadoNexo</h1>
        <p class="cabecera-sub">Estado actual del catálogo de la tienda, resumido en un vistazo.</p>
    </div>
    <div class="cabecera-acciones">
        <a class="btn primario" href="${pageContext.request.contextPath}/productos?action=nuevo">Registrar producto</a>
    </div>
</div>

<div class="estadisticas">
    <div class="estadistica">
        <p class="etiqueta">Productos en catálogo</p>
        <p class="valor">${totalProductos}</p>
        <p class="detalle">Registrados en total</p>
    </div>
    <div class="estadistica">
        <p class="etiqueta">Categorías</p>
        <p class="valor">${totalCategorias}</p>
        <p class="detalle">Grupos del catálogo</p>
    </div>
    <div class="estadistica ${agotados > 0 ? 'alerta' : ''}">
        <p class="etiqueta">Productos agotados</p>
        <p class="valor">${agotados}</p>
        <p class="detalle">Con stock igual a 0</p>
    </div>
    <div class="estadistica">
        <p class="etiqueta">Valor del inventario</p>
        <p class="valor">$ <fmt:formatNumber value="${valorInventario}" pattern="#,##0"/></p>
        <p class="detalle">Precio × stock de todo el catálogo</p>
    </div>
</div>

<div class="panel">
    <h2>Acciones rápidas</h2>
    <div class="rejilla-tarjetas">
        <a class="tarjeta-enlace" href="${pageContext.request.contextPath}/productos">
            <h3>Ver catálogo</h3>
            <p>Consulta, busca y filtra todos los productos registrados.</p>
        </a>
        <a class="tarjeta-enlace" href="${pageContext.request.contextPath}/productos?action=nuevo">
            <h3>Registrar un producto</h3>
            <p>Agrega un nuevo artículo con su categoría, precio y stock.</p>
        </a>
        <a class="tarjeta-enlace" href="${pageContext.request.contextPath}/categorias">
            <h3>Gestionar categorías</h3>
            <p>Crea los grupos que organizan el catálogo de la tienda.</p>
        </a>
    </div>
</div>

<div class="panel">
    <h2>Cómo funciona la aplicación</h2>
    <p class="bloque-texto"><strong>Catálogo.</strong> Desde Productos puedes crear, editar, ver el detalle
       y eliminar artículos. Cada producto pertenece a una categoría y registra precio en pesos chilenos y stock disponible.</p>
    <p class="bloque-texto"><strong>Búsqueda.</strong> El listado admite filtros combinados por nombre y categoría,
       para encontrar un producto sin recorrer toda la tabla.</p>
    <p class="bloque-texto"><strong>Operaciones masivas.</strong> En el catálogo también puedes aplicar ajustes
       de precios por porcentaje o eliminar productos agotados, en una sola acción sobre la base de datos.</p>
</div>

<%@ include file="includes/footer.jspf" %>
