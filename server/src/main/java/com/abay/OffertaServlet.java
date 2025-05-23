package com.abay;

import model.Asta;
import model.Utente;
import utils.DAO.AstaDAO;
import utils.DAO.OffertaDAO;
import model.Offerta;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet("/offerta")
public class OffertaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        // Move errors/success from session to request if present (for PRG)
        if (session != null) {
            Object errors = session.getAttribute("errors");
            if (errors != null) {
                request.setAttribute("errors", errors);
                session.removeAttribute("errors");
            }
            Object success = session.getAttribute("success");
            if (success != null) {
                request.setAttribute("success", success);
                session.removeAttribute("success");
            }
        }

        int astaId = 0;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.sendRedirect("acquisto.jsp");
            return;
        }

        try {
            Asta asta = AstaDAO.getAstaById(astaId); // recupera asta con articoli
            if (asta == null) {
                response.sendRedirect("acquisto.jsp");
                return;
            }
            request.setAttribute("asta", asta);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errors", List.of(e.getMessage()));
        } catch (SQLException e) {
            request.setAttribute("errors", List.of("Errore nel database: " + e.getMessage()));
        }

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
        int prezzoOfferto = Integer.parseInt(request.getParameter("prezzo"));

        Asta asta = null;
        try {
            asta = AstaDAO.getAstaById(astaId);
        } catch (Exception e) {
            response.sendRedirect("acquista");
            return;
        }
        Offerta max = asta.getOffertaMassima();

        List<String> errors = new ArrayList<>();
        request.setAttribute("asta", asta);
        int prezzoMinimo = (max != null ? max.getPrezzo() : asta.getPrezzoIniziale()) + asta.getRialzoMinimo();
        if (prezzoOfferto >= prezzoMinimo) {
            try {
                Offerta offerta = new Offerta();
                offerta.setUsername(username);
                offerta.setPrezzo(prezzoOfferto);
                offerta.setDate(LocalDateTime.now());
                asta.newOfferta(offerta);
                OffertaDAO.registraOfferta(astaId, offerta);
            } catch (IllegalArgumentException e) {
                errors.add("Impossibile registrare offerta: " + e.getMessage());
            } catch (SQLException e) {
                errors.add("Impossibile registrare offerta per errore nel Database: " + e.getMessage());
            }
        } else
            errors.add("L'offerta deve essere maggiore di " + prezzoMinimo + " €");

        if (errors.size() > 0)
            session.setAttribute("errors", errors);
        else
            session.setAttribute("success", "Offerta registrata!");

        // Always redirect to avoid form resubmission
        response.sendRedirect("offerta?id=" + astaId);
    }
}
