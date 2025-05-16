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
    <style>
        .two-column {
            display: flex;
            gap: 20px;
            margin-bottom: 30px;
        }

        .column {
            flex: 1;
            background: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.05);
        }
    </style>
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

    <h1>Gestione Aste</h1>

    <!-- Two column layout for lists -->
    <div class="two-column">
        <!-- Left column - Aste Aperte -->
        <div class="column">
            <h2>Aste Aperte</h2>
            <ul>
                <c:forEach var="a" items="${asteAperte}">
                    <li>
                        <a href="dettaglioAsta?id=${a.id}">
                            <c:if test="${not empty a.immagine}">
                                <img src="data:image/jpeg;base64,${a.immagine}" alt="${a.nome}" width="50" style="margin-right: 10px; vertical-align: middle;"/>
                            </c:if>
                                ${a.nome} –
                                ${a.descrizione} –
                            Articoli: [
                            <c:forEach var="art" items="${a.articoli}">
                                ${art.nome}
                            </c:forEach>
                            ] <c:if test="${a.offertaMassima != null}">
                            – Offerta Max: €${a.offertaMassima.prezzo}
                        </c:if> –
                            Scade tra: ${TimeUtils.getTempoMancante(a.scadenza)}
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </div>

        <!-- Right column - Aste Chiuse -->
        <div class="column">
            <h2>Aste Chiuse</h2>
            <ul>
                <c:forEach var="a" items="${asteChiuse}">
                    <li>
                        <a href="dettaglioAsta?id=${a.id}">
                            <c:if test="${not empty a.immagine}">
                                <img src="data:image/jpeg;base64,${a.immagine}" alt="${a.nome}" width="50" style="margin-right: 10px; vertical-align: middle;"/>
                            </c:if>
                                ${a.nome} –
                            Articoli: [
                            <c:forEach var="art" items="${a.articoli}">
                                ${art.nome}
                            </c:forEach>
                            ]
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>

    <!-- Two column layout for forms -->
    <div class="two-column">
        <!-- Left column - Nuovo Articolo -->
        <div class="column">
            <h2>Nuovo Articolo</h2>
            <form action="vendo" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="createArticolo" />
                <div class="form-group">
                    <label for="nome">Nome:</label>
                    <input type="text" id="nome" name="nome" required>
                </div>
                <div class="form-group">
                    <label for="descrizione">Descrizione:</label>
                    <input type="text" id="descrizione" name="descrizione" required>
                </div>
                <div class="form-group">
                    <label for="immagine">Immagine:</label>
                    <input type="file" id="immagine" name="immagine" accept="image/*" required>
                </div>
                <div class="form-group">
                    <label for="prezzo">Prezzo (€):</label>
                    <input type="number" id="prezzo" name="prezzo" step="0.01" min="0" required>
                </div>
                <button type="submit" class="main-action" style="width:100%; margin:10px 0;">Aggiungi Articolo</button>
            </form>
        </div>

        <!-- Right column - Nuova Asta -->
        <div class="column">
            <h2>Nuova Asta</h2>
            <form action="vendo" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="createAsta" />
                <div class="form-group">
                    <p>Seleziona uno o più articoli:</p>
                    <c:forEach var="art" items="${articoliUtente}">
                        <div>
                            <input type="checkbox" name="articoliId" id="art-${art.id}" value="${art.id}" />
                            <label for="art-${art.id}">${art.nome} (€${art.prezzo})</label>
                        </div>
                    </c:forEach>
                </div>
                <div class="form-group">
                    <label for="asta-nome">Nome:</label>
                    <input type="text" id="asta-nome" name="nome" required>
                </div>
                <div class="form-group">
                    <label for="asta-descrizione">Descrizione:</label>
                    <input type="text" id="asta-descrizione" name="descrizione" required>
                </div>
                <div class="form-group">
                    <label for="asta-immagine">Immagine:</label>
                    <input type="file" id="asta-immagine" name="immagine" accept="image/*" required>
                </div>
                <div class="form-group">
                    <label for="rialzo">Rialzo minimo (€):</label>
                    <input type="number" id="rialzo" name="rialzo" min="1" required>
                </div>
                <div class="form-group">
                    <label for="scadenza">Scadenza:</label>
                    <input type="datetime-local" id="scadenza" name="scadenza" required>
                </div>
                <button type="submit" class="main-action" style="width:100%; margin:10px 0;">Crea Asta</button>
            </form>
        </div>
    </div>
</div>
</body>
</html>