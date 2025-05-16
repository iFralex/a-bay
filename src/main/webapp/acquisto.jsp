<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Pagina Acquisto</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />
<h1>Ricerca Aste</h1>
<form method="get" action="acquisto">
    <input type="text" name="parolaChiave" value="${parolaChiave}" placeholder="Inserisci parola chiave"/>
    <button type="submit">Cerca</button>
</form>

<c:if test="${not empty asteAperte}">
    <h2>Aste aperte trovate</h2>
    <ul>
        <c:forEach var="asta" items="${asteAperte}">
            <li style="display: flex; align-items: flex-start; gap: 12px;">
                <c:if test="${not empty asta.immagine}">
                    <img src="data:image/jpeg;base64,${asta.immagine}" alt="Immagine asta" width="60" height="60" style="object-fit: cover; border-radius: 6px;"/>
                </c:if>
                <div>
                    <a href="offerta?id=${asta.getId()}">
                        Asta #${asta.id}: ${asta.nome} - Scade tra ${asta.tempoRimasto}
                        <br/>Articoli:
                        <c:forEach var="articolo" items="${asta.getArticoli()}" varStatus="status">
                            ${articolo.getNome()}<c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                    </a>
                </div>
            </li>
        </c:forEach>
    </ul>
</c:if>

<jsp:useBean id="user" class="model.Utente" scope="session" />
<c:if test="${user.username ne null}">
    <h2>Offerte aggiudicate</h2>
    <c:if test="${not empty asteAggiudicate}">
        <ul>
            <c:forEach var="asta" items="${asteAggiudicate}">
                <li>
                    Asta #${asta.id} - Articoli:
                    <c:forEach var="art" items="${asta.articoli}">
                        ${art.nome}&nbsp;
                    </c:forEach>
                    <br/>Prezzo finale: €${asta.offertaMassima.prezzo}
                </li>
            </c:forEach>
        </ul>
    </c:if>
    <c:if test="${empty asteAggiudicate}">
        <p>Non hai ancora aggiudicato nessuna offerta.</p>
    </c:if>
</c:if>
</body>
</html>