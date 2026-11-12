<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Categorías"/>
<c:set var="seccionActual" value="categorias"/>

<%@ include file="../includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>Categorías</h1>
        <p class="cabecera-sub">Grupos que organizan el catálogo de productos.</p>
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

<form class="panel" method="post" action="categorias">
    <h2>Registrar categoría</h2>
    <div class="formulario-rejilla">
        <label class="campo">Nombre *
            <input type="text" name="nombre" maxlength="60" required
                   value="<c:out value='${nombreIngresado}'/>" placeholder="Ej: Consolas">
        </label>
        <label class="campo">Descripción
            <input type="text" name="descripcion" maxlength="200"
                   value="<c:out value='${descripcionIngresada}'/>" placeholder="Descripción opcional">
        </label>
    </div>
    <div class="form-acciones">
        <button type="submit" class="btn primario">Registrar categoría</button>
    </div>
    <p class="requisito">* Campo obligatorio.</p>
</form>

<c:choose>
    <c:when test="${empty categorias}">
        <div class="vacio">
            <div class="vacio-icono" aria-hidden="true">◫</div>
            <h2>Aún no hay categorías</h2>
            <p>Registra la primera categoría para poder clasificar los productos del catálogo.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="tabla-scroll">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Descripción</th>
                        <th class="acciones">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="cat" items="${categorias}">
                        <tr>
                            <td class="id-celda" data-etiqueta="ID">${cat.id}</td>
                            <td class="nombre-celda" data-etiqueta="Nombre"><c:out value="${cat.nombre}"/></td>
                            <td data-etiqueta="Descripción"><c:out value="${cat.descripcion}"/></td>
                            <td class="acciones" data-etiqueta="Acciones">
                                <form method="post" action="categorias" class="enlinea"
                                      onsubmit="return confirm('¿Eliminar esta categoría? Solo es posible si no tiene productos.');">
                                    <input type="hidden" name="action" value="eliminar">
                                    <input type="hidden" name="id" value="${cat.id}">
                                    <button type="submit" class="btn mini peligro">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="../includes/footer.jspf" %>
