<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Catálogo de productos"/>
<c:set var="seccionActual" value="productos"/>

<%@ include file="../includes/header.jspf" %>

<div class="cabecera-seccion">
    <div>
        <h1>Catálogo de productos</h1>
        <p class="cabecera-sub">Consulta, crea y administra los productos de la tienda.</p>
    </div>
    <div class="cabecera-acciones">
        <a class="btn primario" href="productos?action=nuevo">Registrar producto</a>
    </div>
</div>

<%-- ---------- Filtros (JPQL parametrizada) ---------- --%>
<form class="panel filtros" method="get" action="productos">
    <input type="hidden" name="action" value="listar">
    <label class="campo">Nombre
        <input type="text" name="nombre" value="<c:out value='${filtroNombre}'/>" placeholder="Buscar por nombre…">
    </label>
    <label class="campo">Categoría
        <select name="categoriaId">
            <option value="">Todas</option>
            <c:forEach var="cat" items="${categorias}">
                <option value="${cat.id}" <c:if test="${filtroCategoriaId == cat.id}">selected</c:if>>
                    <c:out value="${cat.nombre}"/>
                </option>
            </c:forEach>
        </select>
    </label>
    <button type="submit" class="btn">Filtrar</button>
    <a class="btn fantasma" href="productos?action=listar">Limpiar</a>
</form>

<%-- ---------- Operaciones masivas (JPQL UPDATE / DELETE masivo), plegables ---------- --%>
<details class="masivas">
    <summary>Operaciones masivas sobre el catálogo</summary>
    <div class="masivas-cuerpo">
        <form method="post" action="productos" class="masiva-fila">
            <input type="hidden" name="action" value="aumentoPrecios">
            <label class="campo">Categoría
                <select name="masivaCategoriaId">
                    <option value="">Todas</option>
                    <c:forEach var="cat" items="${categorias}">
                        <option value="${cat.id}"><c:out value="${cat.nombre}"/></option>
                    </c:forEach>
                </select>
            </label>
            <label class="campo">Porcentaje %
                <input type="number" name="porcentaje" step="0.1" min="-100" max="100" placeholder="ej: 5 o -10">
            </label>
            <button type="submit" class="btn advertencia">Aplicar ajuste de precios</button>
        </form>
        <div class="masiva-fila">
            <form method="post" action="productos">
                <input type="hidden" name="action" value="eliminarAgotados">
                <button type="submit" class="btn peligro">Eliminar productos sin stock</button>
            </form>
        </div>
        <p class="masivas-nota">Estas acciones modifican varios productos a la vez y no se pueden deshacer.</p>
    </div>
</details>

<%-- ---------- Tabla de productos ---------- --%>
<c:choose>
    <c:when test="${empty productos}">
        <div class="vacio">
            <div class="vacio-icono" aria-hidden="true">◫</div>
            <h2>No hay productos que coincidan</h2>
            <p>Ajusta los filtros de búsqueda o registra el primer producto para comenzar.</p>
            <a class="btn primario" href="productos?action=nuevo">Registrar producto</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="tabla-scroll">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Categoría</th>
                        <th class="num">Precio</th>
                        <th>Stock</th>
                        <th class="acciones">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr>
                            <td class="id-celda" data-etiqueta="ID">${p.id}</td>
                            <td class="nombre-celda" data-etiqueta="Nombre"><c:out value="${p.nombre}"/></td>
                            <td data-etiqueta="Categoría"><span class="chip"><c:out value="${p.categoria.nombre}"/></span></td>
                            <td class="num" data-etiqueta="Precio">$ <fmt:formatNumber value="${p.precio}" pattern="#,##0.00"/></td>
                            <td data-etiqueta="Stock">
                                <c:choose>
                                    <c:when test="${p.stock == 0}">
                                        <span class="estado estado-agotado">Agotado</span>
                                    </c:when>
                                    <c:when test="${p.stock <= 5}">
                                        <span class="estado estado-bajo">Bajo · ${p.stock}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="estado estado-ok">En stock · ${p.stock}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="acciones" data-etiqueta="Acciones">
                                <a class="btn mini" href="productos?action=detalle&id=${p.id}">Ver</a>
                                <a class="btn mini" href="productos?action=editar&id=${p.id}">Editar</a>
                                <a class="btn mini peligro" href="productos?action=confirmar&id=${p.id}">Eliminar</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        <p class="contador">Mostrando ${total} producto(s).</p>
    </c:otherwise>
</c:choose>

<%@ include file="../includes/footer.jspf" %>
