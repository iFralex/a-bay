<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Errore</title>
</head>
<body>
<h1>Si è verificato un errore</h1>

<%-- Get status code --%>
<% Integer statusCode = (Integer)request.getAttribute("jakarta.servlet.error.status_code"); %>
<% Throwable throwable = (Throwable)request.getAttribute("jakarta.servlet.error.exception"); %>
<% String requestUri = (String)request.getAttribute("jakarta.servlet.error.request_uri"); %>

<p><strong>Tipo errore:</strong>
    <% if(statusCode != null) { %>
    Errore HTTP <%= statusCode %>
    <% } else { %>
    Errore applicazione
    <% } %>
</p>

<p><strong>URI richiesta:</strong> <%= requestUri %></p>

<% if(throwable != null) { %>
<p><strong>Messaggio:</strong> <%= throwable.getMessage() %></p>
<details>
    <summary>Dettagli tecnici</summary>
    <pre><%= throwable %></pre>
</details>
<% } %>

<p><a href="${pageContext.request.contextPath}/">Torna alla home page</a></p>
</body>
</html>