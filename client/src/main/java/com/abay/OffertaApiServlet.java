package com.abay;

import model.Asta;
import model.Utente;
import model.Asta.Offerta;
import utils.LocalDateTimeAdapter;
import utils.DAO.AstaDAO;
import utils.DAO.OffertaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.MultipartConfig;

@WebServlet("/api/offerta")
@MultipartConfig
public class OffertaApiServlet extends HttpServlet {
    private static final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        int astaId = 0;
        try {
            astaId = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errors.add("ID asta mancante o non valido");
            result.put("errors", errors);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            Asta asta = AstaDAO.getAstaById(astaId);
            if (asta == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                errors.add("Asta non trovata");
                result.put("errors", errors);
            } else {
                result.put("asta", asta);
            }
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
            result.put("errors", errors);
        } catch (SQLException e) {
            errors.add("Errore nel database: " + e.getMessage());
            result.put("errors", errors);
        }

        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            errors.add("Utente non autenticato");
            result.put("errors", errors);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        String username = utente.getUsername();

        int astaId;
        int prezzoOfferto;
        System.out.println(request.getParameter("astaId") + "   " + request.getParameter("prezzo"));
        try {
            astaId = Integer.parseInt(request.getParameter("astaId"));
            prezzoOfferto = Integer.parseInt(request.getParameter("prezzo"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errors.add("Parametri non validi: " + e.getMessage());
            result.put("errors", errors);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        Asta asta;
        try {
            asta = AstaDAO.getAstaById(astaId);
            if (asta == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                errors.add("Asta non trovata");
                result.put("errors", errors);
                response.getWriter().write(gson.toJson(result));
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            errors.add("Errore nel caricamento asta");
            result.put("errors", errors);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        Offerta max = asta.getOffertaMassima();
        int prezzoMinimo = (max != null ? max.getPrezzo() : asta.getPrezzoIniziale()) + asta.getRialzoMinimo();

        if (prezzoOfferto < prezzoMinimo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errors.add("L'offerta deve essere maggiore di " + prezzoMinimo + " €");
            result.put("errors", errors);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            Offerta offerta = new Offerta(username, prezzoOfferto);
            asta.newOfferta(offerta);
            OffertaDAO.registraOfferta(astaId, offerta);
            result.put("success", "Offerta registrata!");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            errors.add("Impossibile registrare offerta: " + e.getMessage());
            result.put("errors", errors);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            errors.add("Impossibile registrare offerta per errore nel Database: " + e.getMessage());
            result.put("errors", errors);
        }

        response.getWriter().write(gson.toJson(result));
    }
}
