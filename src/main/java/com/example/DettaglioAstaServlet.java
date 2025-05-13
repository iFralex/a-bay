package com.example;

import model.Asta;
import model.Asta.Offerta;
import model.Utente;
import utils.DbManager;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/dettaglioAsta")
public class DettaglioAstaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Recupera ID asta dai parametri
        int astaId = Integer.parseInt(request.getParameter("id"));
        HttpSession session = request.getSession();
        Utente utente = (Utente) session.getAttribute("user");

        // Recupera asta e offerte dal DB
        Asta asta = DbManager.getAstaById(astaId);

        // Calcola se l'asta è scaduta
        boolean chiusa = asta.isChiusa();

        // Inoltra i dati alla JSP
        request.setAttribute("asta", asta);
        request.getRequestDispatcher("/dettaglioAsta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int astaId = Integer.parseInt(request.getParameter("id"));
        DbManager.chiudiAsta(astaId);
        response.sendRedirect("dettaglioAsta?id=" + astaId);
    }
}
