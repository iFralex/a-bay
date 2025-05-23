package com.abay;

import model.Asta;
import model.Utente;
import utils.LocalDateTimeAdapter;
import utils.DAO.AstaDAO;
import utils.DAO.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.servlet.annotation.MultipartConfig;

@WebServlet("/api/dettaglioAsta")
@MultipartConfig
public class DettaglioAstaApiServlet extends HttpServlet {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Utente non autenticato\"}");
            return;
        }

        int astaId;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID asta non valido\"}");
            return;
        }

        try {
            Asta asta = AstaDAO.getAstaById(astaId);
            if (asta == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Asta non trovata\"}");
                return;
            }
            System.out.println(utente.getUsername() + " | " + asta.getVenditore());
            if (!utente.getUsername().equals(asta.getVenditore())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Accesso negato all'asta del venditore\"}");
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("asta", asta);

            if (asta.getOffertaVincitrice() != null) {
                try {
                    Utente vincitore = UtenteDAO.getUtente(asta.getOffertaVincitrice().getUsername());
                    result.put("vincitore", vincitore);
                } catch (IllegalArgumentException e) {
                    result.put("warning", e.getMessage());
                }
            }

            String json = gson.toJson(result);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Errore interno: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Utente non autenticato\"}");
            return;
        }

        int astaId;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID asta non valido\"}");
            return;
        }

        String aggiudicatario = request.getParameter("aggiudicatario");
        if (aggiudicatario == null || aggiudicatario.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Aggiudicatario non specificato\"}");
            return;
        }

        try {
            AstaDAO.chiudiAsta(astaId, aggiudicatario);

            // Ricarica l'asta aggiornata
            Asta asta = AstaDAO.getAstaById(astaId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", "Asta chiusa con successo");
            result.put("asta", asta);

            if (asta.getOffertaVincitrice() != null) {
                try {
                    Utente vincitore = UtenteDAO.getUtente(asta.getOffertaVincitrice().getUsername());
                    result.put("vincitore", vincitore);
                } catch (IllegalArgumentException e) {
                    result.put("warning", e.getMessage());
                }
            }

            String json = gson.toJson(result);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Impossibile chiudere asta: " + e.getMessage() + "\"}");
        }
    }
}
