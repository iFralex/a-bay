<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="utils.TimeUtils" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vendo</title>
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

<h1>Gestione Aste</h1>

<h2>Aste Aperte</h2>
<ul>
    <c:forEach var="a" items="${asteAperte}">
        <li>
            <a href="dettaglioAsta?id=${a.id}">
                ${a.nome} –
                ${a.descrizione} –
                Articoli: [
                <c:forEach var="art" items="${a.articoli}">
                    ${art.nome} 
                </c:forEach>
                ] <c:if test="${a.offertaMassima != null}">
                    – Offerta Max: €${a.offertaMassima.prezzo}
                </c:if>
 –
                Scade tra: ${TimeUtils.getTempoMancante(a.formattedScadenza)}
            </a>
        </li>
    </c:forEach>
</ul>

<h2>Aste Chiuse</h2>
<ul>
    <c:forEach var="a" items="${asteChiuse}">
        <li>
            <a href="dettaglioAsta?id=${a.id}">
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

<hr>

<h2>Nuovo Articolo</h2>
<form action="vendo" method="post">
    <input type="hidden" name="action" value="createArticolo" />
    Nome: <input type="text" name="nome" required><br>
    Descrizione: <input type="text" name="descrizione" required><br>
    Immagine URL: <input type="text" name="immagine" required><br>
    Prezzo (€): <input type="number" name="prezzo" step="0.01" required><br>
    <button type="submit">Aggiungi Articolo</button>
</form>

<h2>Nuova Asta</h2>
<form action="vendo" method="post">
    <input type="hidden" name="action" value="createAsta" />
    <p>Seleziona uno o più articoli:</p>
    <c:forEach var="art" items="${articoliUtente}">
        <input type="checkbox" name="articoliId" value="${art.id}" />
        ${art.nome} (€${art.prezzo})<br>
    </c:forEach>

    Nome: <input type="text" name="nome" required><br>
    Descrizione: <input type="text" name="descrizione" required><br>
    Immagine url: <input type="text" name="immagine" required><br>
    Rialzo minimo (€): <input type="number" name="rialzo" min="1" required><br>
    Scadenza: <input type="datetime-local" name="scadenza" required><br>
    <button type="submit">Crea Asta</button>
</form>

</body>
</html>
