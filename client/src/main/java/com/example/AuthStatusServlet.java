package com.abay;

import model.Utente;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/status")
public class AuthStatusServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");

        HttpSession session = request.getSession(false);
        Utente user = (session != null) ? (Utente) session.getAttribute("user") : null;

        Map<String, Object> result = new HashMap<>();
        result.put("userData", user); // sarà null se non loggato

        response.getWriter().write(gson.toJson(result));
    }
}
