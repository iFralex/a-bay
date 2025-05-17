<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<style>
.message-box {
    max-width: 800px;
    margin: 1.5rem auto;
    padding: 1rem 1.5rem;
    border-radius: 12px;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    font-size: 1rem;
    line-height: 1.4;
    box-shadow: 0 2px 10px rgba(0,0,0,0.08);
    word-break: break-word;
}

.error-box {
    background-color: #ffe6e6;
    color: #b00020;
    border: 1.5px solid #b00020;
}

.error-box strong {
    font-weight: 700;
}

.success-box {
    background-color: #e6f4ea;
    color: #2e7d32;
    border: 1.5px solid #2e7d32;
    font-weight: 600;
}
</style>

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