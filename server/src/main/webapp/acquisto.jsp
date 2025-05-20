<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="model.Utente" %>

<html>
<head>
    <title>Pagina Acquisto</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />

<main class="auction-detail">
    <h1 class="page-title">Ricerca Aste</h1>

    <form method="get" action="acquisto" class="search-form">
        <input type="text" name="parolaChiave" value="${parolaChiave}" placeholder="Inserisci parola chiave" class="form-control" />
        <button type="submit" class="button main-action">Cerca</button>
    </form>

    <jsp:useBean id="asteAperte" scope="request" type="java.util.List"/>
    <c:if test="${not empty asteAperte}">
        <section class="section">
            <div class="section-header">
                <h2>Aste aperte trovate</h2>
            </div>
            <div class="card-grid">
                <c:forEach var="asta" items="${asteAperte}">
                    <a href="offerta?id=${asta.id}" style="color: inherit; text-decoration: none; width: 100%;">
                        <c:set var="asta" value="${asta}" scope="request" />
                        <jsp:include page="templates/astaCard.jsp" />
                    </a>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <c:if test="${not empty sessionScope.user}">
        <section class="section">
            <div class="section-header">
                <h2>Aste vinte</h2>
            </div>
            <jsp:useBean id="asteAggiudicate" scope="request" type="java.util.List"/>
            <c:if test="${not empty asteAggiudicate}">
                <div class="card-grid">
                    <c:forEach var="asta" items="${asteAggiudicate}">
                        <c:set var="asta" value="${asta}" scope="request" />
                        <jsp:include page="templates/astaCard.jsp" />
                    </c:forEach>
                </div>
            </c:if>
            <c:if test="${empty asteAggiudicate}">
                <p class="no-results">Non hai ancora vinto nessuna asta.</p>
            </c:if>
        </section>
    </c:if>
</main>
</body>
</html>