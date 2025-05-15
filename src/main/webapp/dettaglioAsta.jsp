<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Dettaglio Asta</title>
</head>
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
<jsp:useBean id="asta" scope="request" type="model.Asta"/>
<h1>Dettaglio Asta #${asta.id}: ${asta.nome}</h1>

<p><strong>Articoli:</strong></p>
<ul>
    <c:forEach var="articolo" items="${asta.articoli}">
        <li>${articolo.nome} - ${articolo.prezzo} €</li>
    </c:forEach>
</ul>
<p><strong>Venditore:</strong> ${asta.venditore}</p>
<p><strong>Prezzo iniziale:</strong> ${asta.prezzoIniziale} €</p>
<p><strong>Offerta Massima:</strong>
    <c:choose>
        <c:when test="${asta.offertaMassima != null}">
            ${asta.offertaMassima.username} | ${asta.offertaMassima.prezzo} €
        </c:when>
        <c:otherwise>
            -- | -- €
        </c:otherwise>
    </c:choose>
</p>

<p><strong>Rialzo minimo:</strong> ${asta.rialzoMinimo} €</p>
<p><strong>Scadenza:</strong> ${asta.formattedScadenza}</p>
<p><strong>Stato:</strong> <c:choose>
        <c:when test="${asta.chiusa}">Chiusa</c:when>
        <c:otherwise>Aperta</c:otherwise>
    </c:choose>
</p>

<c:if test="${vincitore != null}">
    <p><strong>Vincitore:</strong> ${vincitore.username} | ${vincitore.nome} ${vincitore.cognome}</p>
    <p><strong>Prezzo finale:</strong> ${asta.offertaVincitrice.prezzo} €</p>
    <p><strong>Indirizzo spedizione:</strong> ${vincitore != null ? vincitore.indirizzo : 'Non disponibile'}</p>
</c:if>

<h2>Offerte</h2>
<table border="1">
    <tr>
        <th>Utente</th>
        <th>Importo</th>
        <th>Data/Ora</th>
    </tr>
    <c:forEach var="offerta" items="${asta.offerteSenzaVenditore}">
        <tr>
            <td>${offerta.username}</td>
            <td>${offerta.prezzo} €</td>
            <td>${offerta.formattedDate}</td>
        </tr>
    </c:forEach>
</table>

<c:if test="${!asta.chiusa}">
    <form action="dettaglioAsta" method="post">
        <input type="hidden" name="id" value="${asta.id}" />
        <input type="hidden" name="aggiudicatario" value="${asta.offertaMassima == null ? asta.offertaMassima.username : asta.venditore}" />
        <input type="submit" value="Chiudi Asta" />
    </form>
</c:if>
</c:when>

<c:otherwise>
<p>Asta non visualizzabile o non trovata.</p>
</c:otherwise>
</c:choose>

</body>
</html>
