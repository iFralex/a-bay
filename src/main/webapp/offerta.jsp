<%@ page import="model.*, java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<head><title>Dettaglio Asta</title></head>
<body>
    <c:if test="${not empty errors}">
        <c:forEach var="err" items="${errors}">
            <div style="padding:10px; background-color: #f8d7da; color: #842029; border: 1px solid #f5c2c7; border-radius: 5px; margin-bottom: 15px;">
                <strong>Errore:</strong> ${err}
            </div>
        </c:forEach>
    </c:if>


    <c:if test="${not empty success}">
        <div style="padding:10px; background-color:rgb(116, 203, 116); color:rgb(27, 78, 0); border: 1px solid #f5c2c7; border-radius: 5px; margin-bottom: 15px;">
            <strong>Successo:</strong> ${success}
        </div>
    </c:if>
    
    <c:choose>
    <c:when test="${not empty asta}">
    <h2>Asta #${asta.id}: ${asta.nome}</h2>
    <p>${asta.descrizione}</p>
    <p>Scadenza: ${asta.formattedScadenza}</p>

    <h3>Articoli inclusi:</h3>
    <ul>
        <c:forEach var="articolo" items="${asta.articoli}">
            <li>${articolo.nome} - ${articolo.descrizione}</li>
        </c:forEach>
    </ul>

    <h3>Offerte ricevute:</h3>
    <ul>
        <c:forEach var="offerta" items="${asta.offerteSenzaVenditore}">
            <li>${offerta.username} - €${offerta.prezzo} - ${offerta.formattedDate}</li>
        </c:forEach>
    </ul>

    <jsp:useBean id="user" class="model.Utente" scope="session" />
    <c:if test="${user.username ne null and !asta.chiusa and user.username ne asta.venditore}">
        <h3>Inserisci una nuova offerta:</h3>
        <form method="post" action="offerta">
            <input type="hidden" name="astaId" value="${asta.id}" />
            <label for="prezzo">Prezzo (€) (Rialzo: ${asta.rialzoMinimo}):</label>
            <input type="number" step="1" name="prezzo" id="prezzo" required />
            <button type="submit">Invia Offerta</button>
        </form>
    </c:if>
    </c:when>

<c:otherwise>
<p>Asta non visualizzabile o non trovata.</p>
</c:otherwise>
</c:choose>
</body>
</html>
