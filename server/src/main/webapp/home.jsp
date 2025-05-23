<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Home - Aste Online HTML</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
  <jsp:include page="/templates/navbar.jsp" />
  <jsp:include page="templates/messaggi.jsp" />

  <main class="auction-dashboard">
    <h1 class="page-title">Benvenuto su Aste Online</h1>

    <section class="section">
      <h2 class="section-header">Cosa vuoi fare?</h2>
      <div class="card-grid">
        <a href="vendo" class="card main-action" style="text-align:center; padding: 3rem 1rem; font-size:1.2rem;">
          Vendi un articolo
        </a>
        <a href="acquisto" class="card main-action" style="text-align:center; padding: 3rem 1rem; font-size:1.2rem;">
          Acquista un articolo
        </a>
      </div>
    </section>
  </main>
</body>
</html>