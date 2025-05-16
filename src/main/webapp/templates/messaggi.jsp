<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:if test="${not empty errors}">
    <div class="message-box error-box">
        <c:forEach var="err" items="${errors}">
            <div><strong>Errore:</strong> ${err}</div>
        </c:forEach>
    </div>
</c:if>

<c:if test="${not empty success}">
    <div class="message-box success-box">
        <strong>Successo:</strong> ${success}
    </div>
</c:if>