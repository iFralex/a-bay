<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hello World JSP</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f4f4f4;
            color: #333;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        h1 {
            color: #2c3e50;
        }
        ul {
            list-style-type: circle;
        }
        .technology {
            padding: 5px;
            margin: 5px 0;
            background-color: #e8f4f8;
            border-radius: 3px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Hello World con JSP y JSTL</h1>
        
        <p>Este es un mensaje desde JSP: <strong>¡Hola Mundo!</strong></p>
        
        <c:if test="${not empty message}">
            <p>Y este es el mensaje desde el servlet: <strong>${message}</strong></p>
        </c:if>
        
        <h2>Tecnologías utilizadas:</h2>
        <ul>
            <c:forEach var="tech" items="${technologies}">
                <li class="technology">${tech}</li>
            </c:forEach>
        </ul>
        
        <p>La hora actual es: <c:out value="<%= new java.util.Date() %>" /></p>
    </div>
</body>
</html>