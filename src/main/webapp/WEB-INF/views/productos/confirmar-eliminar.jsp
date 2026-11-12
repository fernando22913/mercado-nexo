<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Eliminar producto"/>
<c:set var="seccionActual" value="productos"/>

<%@ include file="../includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>Eliminar producto</h1>
        <p class="cabecera-sub">Esta acción es permanente y no se puede deshacer.</p>
    </div>
    <div class="cabecera-acciones">
        <a class="btn fantasma" href="productos?action=listar">← Volver al listado</a>
    </div>
</div>

<div class="panel confirmacion">
    <div class="confirmacion-cabecera">
        ¿Está seguro de que desea eliminar el siguiente producto?
    </div>
    <div class="fila-detalle"><span class="etiqueta">ID</span><span>${producto.id}</span></div>
    <div class="fila-detalle"><span class="etiqueta">Nombre</span><span class="nombre-celda"><c:out value="${producto.nombre}"/></span></div>
    <div class="fila-detalle"><span class="etiqueta">Categoría</span><span><span class="chip"><c:out value="${producto.categoria.nombre}"/></span></span></div>
    <div class="fila-detalle"><span class="etiqueta">Precio</span><span class="nombre-celda">$ <fmt:formatNumber value="${producto.precio}" pattern="#,##0.00"/></span></div>
    <div class="fila-detalle"><span class="etiqueta">Stock</span><span>${producto.stock}</span></div>
    <div class="form-acciones">
        <form method="post" action="productos" class="enlinea">
            <input type="hidden" name="action" value="eliminar">
            <input type="hidden" name="id" value="${producto.id}">
            <button type="submit" class="btn peligro">Sí, eliminar producto</button>
        </form>
        <a class="btn fantasma" href="productos?action=detalle&id=${producto.id}">Cancelar</a>
    </div>
</div>

<%@ include file="../includes/footer.jspf" %>
