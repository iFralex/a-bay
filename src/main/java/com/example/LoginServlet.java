package com.example;

import java.io.IOException;

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

        Utente utente = DbManager.getUtente(username);
                
        if (utente != null) {
            boolean isPasswordValid = PasswordUtils.verifyPassword(password, utente.getPasswordHash());

            if (isPasswordValid) {
                HttpSession session = request.getSession();
                session.setAttribute("user", utente);
                session.setAttribute("isLoggedIn", true);
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
        }

        request.setAttribute("errorMessage", "Credenziali non valide!");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
