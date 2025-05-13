<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Dettaglio Asta</title>
</head>
<body>
<jsp:useBean id="asta" scope="request" type="model.Asta"/>

<h1>Dettaglio Asta #${asta.getId()}: ${asta.getNome()}</h1>

<p><strong>Articoli:</strong></p>
<ul>
    <c:forEach var="articolo" items="${asta.articoli}">
        <li>${articolo.getNome()} - ${articolo.getPrezzo()} €</li>
    </c:forEach>
</ul>

<p><strong>Prezzo iniziale:</strong> ${asta.getPrezzoIniziale()} €</p>
<p><strong>Offerta Massima:</strong> ${asta.getOffertaMassima().getUsername()} | ${asta.getOffertaMassima().getPrezzo()} €</p>
<p><strong>Rialzo minimo:</strong> ${asta.getRialzoMinimo()} €</p>
<p><strong>Scadenza:</strong> ${asta.getScadenza().toString()}</p>
<p><strong>Stato:</strong> <c:choose>
        <c:when test="${asta.isChiusa()}">Chiusa</c:when>
        <c:otherwise>Aperta</c:otherwise>
    </c:choose>
</p>

<c:if test="${asta.isChiusa()}">
    <p><strong>Vincitore:</strong> ${asta.getOffertaVincitrice().getUsername()}</p>
    <p><strong>Prezzo finale:</strong> ${asta.getOffertaVincitrice().getPrezzo()} €</p>
    <p><strong>Indirizzo spedizione:</strong> TODO</p>
</c:if>

<h2>Offerte</h2>
<table border="1">
    <tr>
        <th>Utente</th>
        <th>Importo</th>
        <th>Data/Ora</th>
    </tr>
    <c:forEach var="offerta" items="${asta.getOfferte()}">
        <tr>
            <td>${offerta.getUsername()}</td>
            <td>${offerta.getPrezzo()} €</td>
            <td>${offerta.getDate().toString()}</td>
        </tr>
    </c:forEach>
</table>

<c:if test="${!asta.isChiusa()}">
    <form action="dettaglioAsta" method="post">
        <input type="hidden" name="id" value="${asta.id}" />
        <input type="submit" value="Chiudi Asta" />
    </form>
</c:if>

</body>
</html>
