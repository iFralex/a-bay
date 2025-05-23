<jsp:useBean id="exception" scope="request" type="java.nio.charset.MalformedInputException"/>
<%@ page isErrorPage="true" %>
<html>
<head>
    <title>Errore</title>
</head>
<body>
    <h1>Si è verificato un errore</h1>
    <p><strong>Messaggio:</strong> ${exception.message}</p>
    <pre>
        <%= exception.toString() %>
    </pre>
</body>
</html>
