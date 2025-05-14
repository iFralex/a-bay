<%@ page import="model.*, java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head><title>Dettaglio Asta</title></head>
<body>
    <h2>Asta #${asta.id}: ${asta.nome}</h2>
    <p>${asta.descrizione}</p>
    <p>Scadenza: ${asta.scadenza.toString()}</p>

    <h3>Articoli inclusi:</h3>
    <ul>
        <c:forEach var="articolo" items="${asta.articoli}">
            <li>${articolo.nome} - ${articolo.descrizione}</li>
        </c:forEach>
    </ul>

    <h3>Offerte ricevute:</h3>
    <ul>
        <c:forEach var="offerta" items="${asta.offerte}">
            <li>${offerta.username} - €${offerta.prezzo} - ${offerta.date.toString()}</li>
        </c:forEach>
    </ul>

    <h3>Inserisci una nuova offerta:</h3>
    <form method="post" action="offerta">
        <input type="hidden" name="astaId" value="${asta.id}" />
        <label for="prezzo">Prezzo (€) (Rialzo: ${asta.rialzoMinimo}):</label>
        <input type="number" step="1" name="prezzo" required />
        <button type="submit">Invia Offerta</button>
    </form>
</body>
</html>
