package com.abay;

import model.Asta;
import model.Asta.Offerta;
import model.Utente;
import utils.LocalDateTimeAdapter;
import utils.DAO.AstaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/api/acquisto")
public class AcquistoApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        String parolaChiave = request.getParameter("parolaChiave");
        String asteVisitateIdsStr = request.getParameter("asteVisitateIds");
        LocalDateTime now = LocalDateTime.now();

        List<String> errors = new ArrayList<>();
        List<Asta> asteAperte = new ArrayList<>();
        List<Asta> asteAggiudicate = new ArrayList<>();
        List<Asta> asteVisitate = new ArrayList<>();

        // Recupera aste aperte filtrate per parola chiave
        if (parolaChiave != null && !parolaChiave.trim().isEmpty()) {
            try {
                asteAperte = AstaDAO.getAstePerParolaChiave(parolaChiave, now);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            } catch (SQLException e) {
                errors.add("Errore nel database: " + e.getMessage());
            }
        }

        // Ordina aste aperte per tempo rimanente decrescente
        asteAperte.sort(Comparator.comparing(
                (Asta a) -> java.time.Duration.between(now, a.getScadenza()))
                .reversed());

        // Recupera aste aggiudicate per utente loggato (se c'è)
        HttpSession session = request.getSession(false);
        if (session != null) {
            Utente utente = (Utente) session.getAttribute("user");
            if (utente != null) {
                try {
                    try {
                        asteAggiudicate = AstaDAO.getAsteVinteDaUtente(utente.getUsername());
                    } catch (IllegalArgumentException e) {
                        errors.add(e.getMessage());
                    }

                    try {
                        if (asteVisitateIdsStr != null && asteVisitateIdsStr.length() > 0)
                            for (String idStr : asteVisitateIdsStr.split(",")) {
                                Asta asta = AstaDAO.getAstaById(Integer.parseInt(idStr));
                                if (asta != null && !asta.getScadenza().isBefore(now) && !asta.isChiusa())
                                    asteVisitate.add(asta);
                            }
                    } catch (Exception e) {
                        errors.add(e.getMessage());
                    }
                } catch (SQLException e) {
                    errors.add("Errore nel database: " + e.getMessage());
                }
            }
        }

        // Costruisci la risposta JSON
        Map<String, Object> result = new HashMap<>();
        result.put("asteAperte", asteAperte);
        result.put("asteAggiudicate", asteAggiudicate);
        result.put("parolaChiave", parolaChiave);
        result.put("asteVisitate", asteVisitate);
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        String json = gson.toJson(result);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(json);
    }
}
