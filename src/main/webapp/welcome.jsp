<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Welcome</title>
</head>
<body>
<h1>Welcome, ${sessionScope.user}!</h1>
<p>You have successfully logged in.</p>
<a href="${pageContext.request.contextPath}/logout">Logout</a>
</body>
</html>