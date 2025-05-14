package com.example;

import model.Asta;
import model.Utente;
import model.Asta.Offerta;
import utils.DbManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/offerta")
public class OffertaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int astaId = Integer.parseInt(request.getParameter("id"));
        Asta asta = DbManager.getAstaById(astaId); // recupera asta con articoli

        request.setAttribute("asta", asta);
        request.getRequestDispatcher("/offerta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String username = utente.getUsername();
        int astaId = Integer.parseInt(request.getParameter("astaId"));
        double prezzoOfferto = Double.parseDouble(request.getParameter("prezzo"));

        Asta asta = DbManager.getAstaById(astaId);
        Offerta max = asta.getOffertaMassima();

        double prezzoMinimo = max != null ? max.getPrezzo() + asta.getRialzoMinimo() : asta.getRialzoMinimo();
        System.out.print("2prezzoOfferto" + prezzoOfferto);
        if (prezzoOfferto >= prezzoMinimo) {
            System.out.print("p1rezzoOfferto" + prezzoOfferto);
            try {
                System.out.print("prezzoOfferto" + prezzoOfferto);
                DbManager.registraOfferta(astaId, username, prezzoOfferto, LocalDateTime.now());
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        response.sendRedirect("offerta?id=" + astaId);
    }
}
