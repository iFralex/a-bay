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

<jsp:useBean id="asta" scope="request" type="model.Asta"/>
<main class="auction-detail">
    <c:choose>
        <c:when test="${not empty asta}">

            <h1 class="auction-title">Asta #${asta.id}: ${asta.nome}</h1>

            <div class="auction-info">
                <c:if test="${not empty asta.immagine}">
                    <img class="auction-image" src="data:image/jpeg;base64,${asta.immagine}" alt="Immagine asta" />
                </c:if>
                <p class="auction-description">${asta.descrizione}</p>
                <p><strong>Scadenza:</strong> ${asta.formattedScadenza}</p>
            </div>

            <section class="section">
                <div class="section-header">
                    <h2>Articoli inclusi</h2>
                </div>
                <div class="scroll-row">
                    <c:forEach var="articolo" items="${asta.articoli}">
                        <div class="scroll-item">
                            <c:set var="articolo" value="${articolo}" scope="request" />
                            <jsp:include page="templates/articoloCard.jsp" />
                        </div>
                    </c:forEach>
                </div>
            </section>

            <section class="section">
                <div class="section-header">
                    <h2>Offerte ricevute</h2>
                </div>
                <table class="offerte-table">
                    <thead>
                        <tr>
                            <th>Utente</th>
                            <th>Importo</th>
                            <th>Data/Ora</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="offerta" items="${asta.getOfferteSenzaVenditore(true)}">
                            <tr>
                                <td>${offerta.username}</td>
                                <td>€${offerta.prezzo}</td>
                                <td>${offerta.formattedDate}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </section>

            <c:if test="${user.username ne null and !asta.chiusa and user.username ne asta.venditore}">
                <section class="section">
                    <div class="section-header">
                        <h2>Inserisci una nuova offerta</h2>
                    </div>
                    <form method="post" action="offerta" class="form">
                        <input type="hidden" name="astaId" value="${asta.id}" />
                        <div class="form-group">
                            <label for="prezzo">Prezzo (€) (Rialzo minimo: €${asta.rialzoMinimo}):</label>
                            <input type="number" step="1" name="prezzo" id="prezzo" required class="form-control" />
                        </div>
                        <button type="submit" class="button main-action">Invia Offerta</button>
                    </form>
                </section>
            </c:if>

        </c:when>
        <c:otherwise>
            <p class="not-found">Asta non visualizzabile o non trovata.</p>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
