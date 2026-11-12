<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Error inesperado"/>

<%@ include file="../includes/header.jspf" %>

<div class="vacio">
    <div class="vacio-icono" aria-hidden="true">!</div>
    <h1>Error inesperado</h1>
    <p>Ocurrió un problema al procesar la solicitud. Verifique que PostgreSQL esté en ejecución
       (docker compose up -d) e intente nuevamente.</p>
    <a class="btn primario" href="${pageContext.request.contextPath}/productos">Volver al catálogo</a>
</div>

<%@ include file="../includes/footer.jspf" %>
