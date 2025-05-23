package com.abay;

import model.Asta;
import model.Offerta;
import model.Utente;
import utils.DAO.AstaDAO;
import utils.DAO.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@WebServlet("/dettaglioAsta")
public class DettaglioAstaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if (session.getAttribute("errors") != null) {
            request.setAttribute("errors", session.getAttribute("errors"));
            session.removeAttribute("errors");
        }
        if (session.getAttribute("success") != null) {
            request.setAttribute("success", session.getAttribute("success"));
            session.removeAttribute("success");
        }

        // Recupera ID asta dai parametri
        int astaId = 0;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.sendRedirect("vendo.jsp");
            return;
        }

        List<String> errors = new ArrayList<>();

        // Recupera asta e offerte dal DB
        Asta asta = null;
        try {
            asta = AstaDAO.getAstaById(astaId);
            if (asta == null) {
                response.sendRedirect("vendo.jsp");
                return;
            }

//            if (Objects.equals(utente.getUsername(), asta.getVenditore())) {
//                response.sendRedirect("vendo.jsp");
//                return;
//            }

            // Se l'asta è chiusa, recupera l'utente vicncitore
            if (asta.getOffertaVincitrice() != null) {
                try {
                    Utente vincitore = UtenteDAO.getUtente(asta.getOffertaVincitrice().getUsername());
                    request.setAttribute("vincitore", vincitore);
                } catch (IllegalArgumentException e) {
                    errors.add(e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        } catch (SQLException e) {
            errors.add("Errore nel Database: " + e.getMessage());
        }

        // Inoltra i dati alla JSP
        request.setAttribute("asta", asta);
        if (errors.size() > 0)
            request.setAttribute("errors", errors);
        request.getRequestDispatcher("/dettaglioAsta.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int astaId;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.sendRedirect("vendo.jsp");
            return;
        }

        String aggiudicatario = request.getParameter("aggiudicatario");
        List<String> errors = new ArrayList<>();
        Asta asta = null;

        try {
            AstaDAO.chiudiAsta(astaId, aggiudicatario);
            request.setAttribute("success", "Asta chiusa!");
        } catch (SQLException e) {
            errors.add("Impossibile chiudere asta per un errore nel Database: " + e.getMessage());
        }

        try {
            asta = AstaDAO.getAstaById(astaId);
            if (asta != null && asta.getOffertaVincitrice() != null) {
                try {
                    Utente vincitore = UtenteDAO.getUtente(asta.getOffertaVincitrice().getUsername());
                    request.setAttribute("vincitore", vincitore);
                } catch (IllegalArgumentException e) {
                    errors.add(e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Errore nel caricamento asta: " + e.getMessage());
        }

        request.setAttribute("asta", asta);
        if (!errors.isEmpty())
            request.setAttribute("errors", errors);

        // request.getRequestDispatcher("/dettaglioAsta.jsp").forward(request,
        // response);
        response.sendRedirect("dettaglioAsta?id=" + astaId);

    }
}
