<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
  <jsp:include page="templates/messaggi.jsp" />

  <div class="auction-dashboard">
    <h1 class="page-title">Login</h1>

    <section class="">
      <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
        <div class="form-group">
          <label for="username">Username:</label>
          <input type="text" id="username" name="username" required />
        </div>

        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" name="password" required />
        </div>

        <button type="submit" class="main-action" style="width: 100%; margin-top: 1rem;">
          Accedi
        </button>
      </form>
    </section>
  </div>
</body>
</html>
