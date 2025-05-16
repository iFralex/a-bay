<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Home - Aste Online</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<jsp:include page="/templates/navbar.jsp" />
<jsp:include page="templates/messaggi.jsp" />
<div class="home-container">
    <h1>Benvenuto su Aste Online</h1>
    <h2>Cosa vuoi fare?</h2>
    <a href="vendo" class="main-action">Vendi un articolo</a>
    <a href="acquisto" class="main-action">Acquista un articolo</a>
</div>
</body>
</html>
