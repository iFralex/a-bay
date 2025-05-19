package com.abay;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.Utente;
import utils.DbManager;
import utils.PasswordUtils;

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

        Utente utente = null;

        try {
            utente = DbManager.getUtente(username);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errors", List.of("Errore nel recuperare l'utente: " + e.getMessage()));
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        } catch (SQLException e) {
            request.setAttribute("errors", List.of("Errore neldatabase: " + e.getMessage()));
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (utente != null) {
            boolean isPasswordValid = PasswordUtils.verifyPassword(password, utente.getPasswordHash());

            if (isPasswordValid) {
                HttpSession session = request.getSession();
                session.setAttribute("user", utente);
                session.setAttribute("isLoggedIn", true);
                response.sendRedirect(request.getContextPath() + "/index.html");
                return;
            }
        }

        request.setAttribute("errors", List.of("Credenziali non valide!"));
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
