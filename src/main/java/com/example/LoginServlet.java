package com.example;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import utils.DbManager;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Simulazione di autenticazione (in produzione usare autenticazione reale)
        if ("admin".equals(username) && "password".equals(password)) {
            // Autenticazione riuscita
            HttpSession session = request.getSession();
            session.setAttribute("user", DbManager.getUtente(username));
            session.setAttribute("isLoggedIn", true);

            // Reindirizzamento alla pagina di benvenuto
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            // Autenticazione fallita
            request.setAttribute("errorMessage", "Credenziali non valide!");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}