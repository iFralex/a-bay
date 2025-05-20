<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Dettaglio Asta</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />

<main class="auction-detail">
    <c:choose>

        <jsp:useBean id="asta" scope="request" type="model.Asta"/>
        <c:when test="${not empty asta}">
            <h1 class="auction-title">Asta #${asta.id}: ${asta.nome}</h1>
            <c:if test="${not empty asta.immagine}">
                <img class="auction-image" src="data:image/jpeg;base64,${asta.immagine}" alt="Immagine asta" />
            </c:if>
            <p class="auction-description">${asta.descrizione}</p>

            <div class="info-grid">
                <div><strong>Venditore:</strong> ${asta.venditore}</div>
                <div><strong>Prezzo iniziale:</strong> €${asta.prezzoIniziale}</div>
                <div>
                    <strong>Offerta Massima:</strong>
                    <c:choose>
                        <c:when test="${asta.offertaMassima != null}">
                            ${asta.offertaMassima.username} | €${asta.offertaMassima.prezzo}
                        </c:when>
                        <c:otherwise>-- | --</c:otherwise>
                    </c:choose>
                </div>
                <div><strong>Rialzo minimo:</strong> €${asta.rialzoMinimo}</div>
                <div><strong>Scadenza:</strong> ${asta.formattedScadenza}</div>
                <div>
                    <strong>Stato:</strong> 
                    <span class="badge ${asta.chiusa ? 'closed' : 'open'}">
                        <c:choose>
                            <c:when test="${asta.chiusa}">Chiusa</c:when>
                            <c:otherwise>Aperta</c:otherwise>
                        </c:choose>
                    </span>
                </div>
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

            <jsp:useBean id="vincitore" scope="request" type="model.Utente"/>
            <c:if test="${vincitore != null}">
                <section class="winner-section">
                    <h3>Vincitore</h3>
                    <p><strong>Username:</strong> ${vincitore.username}</p>
                    <p><strong>Nome:</strong> ${vincitore.nome} ${vincitore.cognome}</p>
                    <p><strong>Prezzo finale:</strong> €${asta.offertaVincitrice.prezzo}</p>
                    <p><strong>Indirizzo:</strong> ${vincitore.indirizzo}</p>
                    <p><strong>Costo spedizione:</strong> €5</p>
                </section>
            </c:if>

            <section class="offerte-section">
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

            <c:if test="${!asta.chiusa}">
                <form class="chiudi-asta-form" method="post" action="dettaglioAsta">
                    <input type="hidden" name="id" value="${asta.id}" />
                    <input type="hidden" name="aggiudicatario" value="${asta.offertaMassima != null ? asta.offertaMassima.username : asta.venditore}" />
                    <button type="submit">Chiudi Asta</button>
                </form>
            </c:if>
        </c:when>

        <c:otherwise>
            <p class="not-found">Asta non trovata o non visualizzabile.</p>
        </c:otherwise>
    </c:choose>
</main>
</body>
