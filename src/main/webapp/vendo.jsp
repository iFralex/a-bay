<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.*, java.util.*, utils.TimeUtils" %>
<%
    Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;
    if (utente == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    List<Articolo> articoliUtente = (List<Articolo>) request.getAttribute("articoliUtente");
    List<Asta> asteAperte = (List<Asta>) request.getAttribute("asteAperte");
    List<Asta> asteChiuse = (List<Asta>) request.getAttribute("asteChiuse");
%>
<% out.println("asteAperte: " + asteAperte); %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vendo</title>
</head>
<body>
<h1>Gestione Aste</h1>

<h2>Aste Aperte</h2>
<ul>
<% for (Asta a : asteAperte) { %>
    <li>
        <a href="dettaglioAsta?id=<%= a.getId() %>">
            <%= a.getNome() %> –
            <%= a.getDescrizione() %> – 
            Articoli: [
            <% for (Articolo art : a.getArticoli()) { %>
                <%= art.getNome() %> 
            <% } %>
            ] – Offerta Max: €<%= a.getOffertaMassima().getPrezzo() %> –
            Scade tra: <%= TimeUtils.getTempoMancante(a.getScadenza()) %>
        </a>
    </li>
<% } %>
</ul>

<h2>Aste Chiuse</h2>
<ul>
<% for (Asta a : asteChiuse) { %>
    <li>
        <a href="dettaglioAsta?id=<%= a.getId() %>">
            <%= a.getNome() %> –
            Articoli: [
            <% for (Articolo art : a.getArticoli()) { %>
                <%= art.getNome() %>
            <% } %>
            ]
        </a>
    </li>
<% } %>
</ul>

<hr>

<h2>Nuovo Articolo</h2>
<form action="creaArticolo" method="post">
    Nome: <input type="text" name="nome" required><br>
    Descrizione: <input type="text" name="descrizione" required><br>
    Immagine URL: <input type="text" name="immagine" required><br>
    Prezzo (€): <input type="number" name="prezzo" step="0.01" required><br>
    <button type="submit">Aggiungi Articolo</button>
</form>

<hr>

<h2>Nuova Asta</h2>
<form action="creaAsta" method="post">
    <p>Seleziona uno o più articoli:</p>
    <% for (Articolo art : articoliUtente) { %>
        <input type="checkbox" name="articoliId" value="<%= art.getId() %>">
        <%= art.getNome() %> (€<%= art.getPrezzo() %>)<br>
    <% } %>
    <% if (request.getAttribute("errorMessage") != null) { %>
    <div style="color: red; margin: 10px 0;">
        <%= request.getAttribute("errorMessage") %>
    </div>
    <% } %>
    Nome: <input type="text" name="nome" required><br>
    Descrizione: <input type="text" name="descrizione" required><br>
    Immagine url: <input type="text" name="immagine" required><br>
    Rialzo minimo (€): <input type="number" name="rialzo" min="1" required><br>
    Scadenza (es. 2025-05-15T23:59): <input type="datetime-local" name="scadenza" required><br>
    <button type="submit">Crea Asta</button>
</form>

</body>
</html>
