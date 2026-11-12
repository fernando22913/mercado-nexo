<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Página no encontrada"/>
<%@ include file="../includes/header.jspf" %>

<div class="vacio">
    <div class="vacio-icono" aria-hidden="true">404</div>
    <h1>Página no encontrada</h1>
    <p>La dirección solicitada no existe en la aplicación. Puedes volver al catálogo de productos.</p>
    <a class="btn primario" href="${pageContext.request.contextPath}/productos">Volver al catálogo</a>
</div>

<%@ include file="../includes/footer.jspf" %>
