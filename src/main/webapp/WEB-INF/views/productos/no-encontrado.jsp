<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Producto no encontrado"/>
<c:set var="seccionActual" value="productos"/>

<%@ include file="../includes/header.jspf" %>

<div class="vacio">
    <div class="vacio-icono" aria-hidden="true">◌</div>
    <h1>Producto no encontrado</h1>
    <p>El producto solicitado no existe o fue eliminado. Revisa el catálogo para ver los artículos disponibles.</p>
    <a class="btn primario" href="productos?action=listar">Volver al catálogo</a>
</div>

<%@ include file="../includes/footer.jspf" %>
