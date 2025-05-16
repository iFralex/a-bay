<%@ page import="model.*, java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dettaglio Asta</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />
<div class="auction-container">
    
    <c:choose>
        <c:when test="${not empty asta}">
            <h1>Asta #${asta.id}: ${asta.nome}</h1>

            <div class="auction-details">
                <p>${asta.descrizione}</p>
                <p><strong>Scadenza:</strong> ${asta.formattedScadenza}</p>

                <h2>Articoli inclusi:</h2>
                <ul>
                    <c:forEach var="articolo" items="${asta.articoli}">
                        <li>${articolo.nome} - ${articolo.descrizione}</li>
                    </c:forEach>
                </ul>

                <h2>Offerte ricevute:</h2>
                <table class="auction-table">
                    <tr>
                        <th>Utente</th>
                        <th>Importo</th>
                        <th>Data/Ora</th>
                    </tr>
                    <c:forEach var="offerta" items="${asta.offerteSenzaVenditore}">
                        <tr>
                            <td>${offerta.username}</td>
                            <td>€${offerta.prezzo}</td>
                            <td>${offerta.formattedDate}</td>
                        </tr>
                    </c:forEach>
                </table>

                <jsp:useBean id="user" class="model.Utente" scope="session" />
                <c:if test="${user.username ne null and !asta.chiusa and user.username ne asta.venditore}">
                    <h2>Inserisci una nuova offerta:</h2>
                    <form method="post" action="offerta">
                        <input type="hidden" name="astaId" value="${asta.id}" />
                        <div class="form-group">
                            <label for="prezzo">Prezzo (€) (Rialzo minimo: €${asta.rialzoMinimo}):</label>
                            <input type="number" step="1" name="prezzo" id="prezzo" required />
                        </div>
                        <button type="submit" class="main-action" style="width:100%; margin:10px 0;">Invia Offerta</button>
                    </form>
                </c:if>
            </div>
        </c:when>

        <c:otherwise>
            <p>Asta non visualizzabile o non trovata.</p>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>