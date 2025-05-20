<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="utils.TimeUtils" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vendo</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />
<div class="auction-dashboard">
    <h1 class="page-title">Gestione Aste</h1>

    <!-- Sezione Aste -->
    <div class="section">
        <div class="section-header">
            <h2>Aste Attive</h2>
        </div>
        <div class="card-grid">
            <jsp:useBean id="asteAperte" scope="request" type="java.util.List"/>
            <c:forEach var="a" items="${asteAperte}">
                <a href="dettaglioAsta?id=${a.id}" class="grid" style="color: inherit; text-decoration: none; width: 100%; display: block;">
                    <c:set var="asta" value="${a}" scope="request" />
                    <jsp:include page="templates/astaCard.jsp" />
                </a>
            </c:forEach>
        </div>

        <div class="section-header">
            <h2>Aste Chiuse</h2>
        </div>
        <div class="card-grid">
            <jsp:useBean id="asteChiuse" scope="request" type="java.util.List"/>
            <c:forEach var="a" items="${asteChiuse}">
                <a href="dettaglioAsta?id=${a.id}" class="grid" style="color: inherit; text-decoration: none; width: 100%;">
                    <c:set var="asta" value="${a}" scope="request" />
                    <jsp:include page="templates/astaCard.jsp" />
                </a>
            </c:forEach>
        </div>
    </div>

    <!-- Sezione Forms -->
    <div class="section forms-section">
        <div class="form-card">
            <h2>Nuovo Articolo</h2>
            <form action="vendo" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="createArticolo" />
                <label>Nome</label>
                <input type="text" name="nome" required />
                <label>Descrizione</label>
                <textarea name="descrizione" required></textarea>
                <label>Immagine</label>
                <input type="file" name="immagine" accept="image/*" required />
                <label>Prezzo (€)</label>
                <input type="number" name="prezzo" step="0.01" min="0" required />
                <button type="submit" class="main-action">Aggiungi Articolo</button>
            </form>
        </div>

        <div class="form-card">
            <h2>Nuova Asta</h2>
            <form action="vendo" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="createAsta" />
                <label>Articoli da includere</label>
                <div class="scroll-row">
                    <jsp:useBean id="articoliUtente" scope="request" type="java.util.List"/>
                    <c:forEach var="art" items="${articoliUtente}">
                        <div class="scroll-item">
                            <input type="checkbox" name="articoliId" id="art-${art.id}" value="${art.id}" />
                            <label for="art-${art.id}">
                                <c:set var="articolo" value="${art}" scope="request" />
                                <jsp:include page="templates/articoloCard.jsp" />
                            </label>
                        </div>
                    </c:forEach>
                </div>

                <label>Nome</label>
                <input type="text" name="nome" required />
                <label>Descrizione</label>
                <textarea name="descrizione" required></textarea>
                <label>Immagine</label>
                <input type="file" name="immagine" accept="image/*" required />
                <label>Rialzo minimo (€)</label>
                <input type="number" name="rialzo" min="1" required />
                <label>Scadenza</label>
                <input type="datetime-local" name="scadenza" required />
                <button type="submit" class="main-action">Crea Asta</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
