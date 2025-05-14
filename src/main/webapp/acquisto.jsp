<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Pagina Acquisto</title>
</head>
<body>
    <h1>Ricerca Aste</h1>
    <form method="get" action="acquisto">
        <input type="text" name="parolaChiave" value="${parolaChiave}" placeholder="Inserisci parola chiave"/>
        <button type="submit">Cerca</button>
    </form>

    <c:if test="${not empty asteAperte}">
        <h2>Aste aperte trovate</h2>
        <ul>
            <c:forEach var="asta" items="${asteAperte}">
                <li>
                    <a href="offerta?idAsta=${asta.getId()}">
                        Asta #${asta.id}: ${asta.nome} - Scade tra ${asta.tempoRimasto}
                        <br/>Articoli: <c:forEach var="articolo" items="${asta.getArticoli()}">${articolo.getNome()}, </c:forEach>
                    </a>
                </li>
            </c:forEach>
        </ul>
    </c:if>

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
    <c:if test="${empty offerteAggiudicate}">
        <p>Non hai ancora aggiudicato nessuna offerta.</p>
    </c:if>
</body>
</html>
