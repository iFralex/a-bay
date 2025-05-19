<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ page session="true" %>
<nav>
    <style>
        nav {
            background-color: #007bff;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 20px;
            font-family: Arial, sans-serif;
        }

        .left, .center, .right {
            display: flex;
            align-items: center;
        }

        .left a, .right a {
            color: white;
            text-decoration: none;
            margin-right: 15px;
            font-weight: bold;
        }

        .left a:hover, .right a:hover {
            text-decoration: underline;
        }

        form {
            display: flex;
        }

        input[type="text"] {
            padding: 5px;
            border: none;
            border-radius: 3px 0 0 3px;
        }

        button {
            padding: 5px 10px;
            border: none;
            background-color: #0056b3;
            color: white;
            border-radius: 0 3px 3px 0;
            cursor: pointer;
        }

        button:hover {
            background-color: #003f7f;
        }
    </style>

    <div class="left">
        <a href="/">a-bay</a>
        <a href="/vendo">Vendi</a>
        <a href="/acquisto">Acquista</a>
    </div>

    <div class="center">
        <form method="get" action="/acquisto">
            <input type="text" name="parolaChiave" placeholder="Cerca...">
            <button type="submit">Cerca</button>
        </form>
    </div>

    <div class="right">
        <c:choose>
            <c:when test="${not empty sessionScope.user}">
                <span><c:out value="${sessionScope.user.nome}"/> <c:out value="${sessionScope.user.cognome}"/></span>
                <a href="/logout">Logout</a>
            </c:when>
            <c:otherwise>
                <a href="/login">Login</a>
            </c:otherwise>
        </c:choose>
    </div>
</nav>
