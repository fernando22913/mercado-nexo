<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Detalle del producto"/>
<c:set var="seccionActual" value="productos"/>

<%@ include file="../includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>Detalle del producto</h1>
        <p class="cabecera-sub">Ficha completa del artículo seleccionado.</p>
    </div>
    <div class="cabecera-acciones">
        <a class="btn fantasma" href="productos?action=listar">← Volver al listado</a>
    </div>
</div>

<div class="panel detalle">
    <div class="fila-detalle"><span class="etiqueta">ID</span><span>${producto.id}</span></div>
    <div class="fila-detalle"><span class="etiqueta">Nombre</span><span class="nombre-celda"><c:out value="${producto.nombre}"/></span></div>
    <div class="fila-detalle"><span class="etiqueta">Categoría</span><span><span class="chip"><c:out value="${producto.categoria.nombre}"/></span></span></div>
    <div class="fila-detalle"><span class="etiqueta">Descripción categoría</span><span><c:out value="${producto.categoria.descripcion}"/></span></div>
    <div class="fila-detalle"><span class="etiqueta">Precio</span><span class="nombre-celda">$ <fmt:formatNumber value="${producto.precio}" pattern="#,##0.00"/></span></div>
    <div class="fila-detalle"><span class="etiqueta">Stock</span>
        <span>
            <c:choose>
                <c:when test="${producto.stock == 0}">
                    <span class="estado estado-agotado">Agotado</span>
                </c:when>
                <c:when test="${producto.stock <= 5}">
                    <span class="estado estado-bajo">Bajo · ${producto.stock}</span>
                </c:when>
                <c:otherwise>
                    <span class="estado estado-ok">En stock · ${producto.stock}</span>
                </c:otherwise>
            </c:choose>
        </span>
    </div>
    <div class="form-acciones">
        <a class="btn primario" href="productos?action=editar&id=${producto.id}">Editar</a>
        <a class="btn peligro" href="productos?action=confirmar&id=${producto.id}">Eliminar</a>
        <a class="btn fantasma" href="productos?action=listar">Volver</a>
    </div>
</div>

<%@ include file="../includes/footer.jspf" %>
