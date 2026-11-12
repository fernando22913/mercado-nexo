<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
    <c:when test="${empty producto.id}">
        <c:set var="tituloPagina" value="Nuevo producto"/>
        <c:set var="seccionActual" value="productos"/>
        <c:set var="accionForm" value="guardar"/>
    </c:when>
    <c:otherwise>
        <c:set var="tituloPagina" value="Editar producto"/>
        <c:set var="seccionActual" value="productos"/>
        <c:set var="accionForm" value="guardar"/>
    </c:otherwise>
</c:choose>

<%@ include file="../includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>${empty producto.id ? 'Registrar nuevo producto' : 'Editar producto'}</h1>
        <p class="cabecera-sub">${empty producto.id ? 'Completa los datos del artículo para agregarlo al catálogo.' : 'Modifica los datos del producto y guarda los cambios.'}</p>
    </div>
    <div class="cabecera-acciones">
        <a class="btn fantasma" href="productos?action=listar">← Volver al listado</a>
    </div>
</div>

<c:if test="${not empty errores}">
    <div class="mensaje error" role="alert">
        <span class="mensaje-cuerpo">
            <strong>Corrija los siguientes datos:</strong>
            <ul>
                <c:forEach var="e" items="${errores}">
                    <li><c:out value="${e}"/></li>
                </c:forEach>
            </ul>
        </span>
    </div>
</c:if>

<form class="panel formulario" method="post" action="productos">
    <input type="hidden" name="action" value="${accionForm}">
    <input type="hidden" name="id" value="${producto.id}">

    <div class="formulario-rejilla">
        <label class="campo campo-completo">Nombre *
            <input type="text" name="nombre" maxlength="100" required
                   value="<c:out value='${producto.nombre}'/>" placeholder="Ej: Teclado mecánico K7">
        </label>

        <label class="campo">Categoría *
            <select name="categoriaId" required>
                <option value="">Seleccione…</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" <c:if test="${producto.categoria.id == cat.id}">selected</c:if>>
                        <c:out value="${cat.nombre}"/>
                    </option>
                </c:forEach>
            </select>
        </label>

        <label class="campo">Precio (CLP) *
            <input type="text" name="precio" required inputmode="decimal"
                   value="${not empty producto.precio ? producto.precio : ''}" placeholder="Ej: 19990 o 19990,50">
        </label>

        <label class="campo">Stock *
            <input type="number" name="stock" min="0" required
                   value="${not empty producto.stock ? producto.stock : 0}">
        </label>
    </div>

    <div class="form-acciones">
        <button type="submit" class="btn primario">${empty producto.id ? 'Registrar producto' : 'Guardar cambios'}</button>
        <a class="btn fantasma" href="productos?action=listar">Cancelar</a>
    </div>
    <p class="requisito">* Campos obligatorios.</p>
</form>

<%@ include file="../includes/footer.jspf" %>
