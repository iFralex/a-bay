package com.example;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("isLoggedIn") != null && 
                (Boolean) session.getAttribute("isLoggedIn")) {
            // L'utente è autenticato
            request.getRequestDispatcher("/home.jsp").forward(request, response);
        } else {
            // L'utente non è autenticato, reindirizzamento alla pagina di login
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}