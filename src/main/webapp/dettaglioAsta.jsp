<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Dettaglio Asta</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<div class="auction-container">
    <c:if test="${not empty errors}">
        <c:forEach var="err" items="${errors}">
            <div class="error-message">
                <strong>Errore:</strong> ${err}
            </div>
        </c:forEach>
    </c:if>

    <c:if test="${not empty success}">
        <div class="success-message">
            <strong>Successo:</strong> ${success}
        </div>
    </c:if>

    <c:choose>
        <c:when test="${not empty asta}">
            <jsp:useBean id="asta" scope="request" type="model.Asta"/>
            <div class="auction-header">
                <h1>Dettaglio Asta #${asta.id}: ${asta.nome}</h1>
            </div>

            <div class="auction-details">
                <p><strong>Articoli:</strong></p>
                <ul>
                    <c:forEach var="articolo" items="${asta.articoli}">
                        <li>${articolo.nome} - ${articolo.prezzo} &euro;</li>
                    </c:forEach>
                </ul>
                <p><strong>Venditore:</strong> ${asta.venditore}</p>
                <p><strong>Prezzo iniziale:</strong> ${asta.prezzoIniziale} &euro;</p>
                <p><strong>Offerta Massima:</strong>
                    <c:choose>
                        <c:when test="${asta.offertaMassima != null}">
                            ${asta.offertaMassima.username} | ${asta.offertaMassima.prezzo} &euro;
                        </c:when>
                        <c:otherwise>
                            -- | -- &euro;
                        </c:otherwise>
                    </c:choose>
                </p>
                <p><strong>Rialzo minimo:</strong> ${asta.rialzoMinimo} &euro;</p>
                <p><strong>Scadenza:</strong> ${asta.formattedScadenza}</p>
                <p><strong>Stato:</strong> <span class="status-${asta.chiusa ? 'closed' : 'open'}">
            <c:choose>
                <c:when test="${asta.chiusa}">Chiusa</c:when>
                <c:otherwise>Aperta</c:otherwise>
            </c:choose>
        </span></p>
            </div>

            <c:if test="${vincitore != null}">
                <div class="winner-box">
                    <p><strong>Vincitore:</strong> ${vincitore.username} | ${vincitore.nome} ${vincitore.cognome}</p>
                    <p><strong>Prezzo finale:</strong> ${asta.offertaVincitrice.prezzo} &euro;</p>
                    <p><strong>Indirizzo spedizione:</strong> ${vincitore != null ? vincitore.indirizzo : 'Non disponibile'}</p>
                </div>
            </c:if>

            <h2>Offerte</h2>
            <table class="auction-table">
                <tr>
                    <th>Utente</th>
                    <th>Importo</th>
                    <th>Data/Ora</th>
                </tr>
                <c:forEach var="offerta" items="${asta.offerteSenzaVenditore}">
                    <tr>
                        <td>${offerta.username}</td>
                        <td>${offerta.prezzo} &euro;</td>
                        <td>${offerta.formattedDate}</td>
                    </tr>
                </c:forEach>
            </table>

            <c:if test="${!asta.chiusa}">
                <form action="dettaglioAsta" method="post" class="close-auction-form">
                    <input type="hidden" name="id" value="${asta.id}" />
                    <input type="hidden" name="aggiudicatario" value="${asta.offertaMassima != null ? asta.offertaMassima.username : asta.venditore}" />
                    <input type="submit" value="Chiudi Asta" />
                </form>
            </c:if>
        </c:when>

        <c:otherwise>
            <p>Asta non visualizzabile o non trovata.</p>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>