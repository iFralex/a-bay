package com.abay;

import model.Asta;
import model.Utente;
import model.Articolo;
import model.Asta.Offerta;
import utils.DbManager;
import utils.LocalDateTimeAdapter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
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

@WebServlet("/api/vendo")
@MultipartConfig
public class VendoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Accesso negato. Effettua il login.\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();

        try {
            List<Articolo> articoliUtente = DbManager.getArticoliDisponibiliPerUtente(utente.getUsername());
            List<Asta> asteAperte = DbManager.getAsteUtente(utente.getUsername(), false);
            List<Asta> asteChiuse = DbManager.getAsteUtente(utente.getUsername(), true);

            result.put("articoliUtente", articoliUtente);
            result.put("asteAperte", asteAperte);
            result.put("asteChiuse", asteChiuse);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("error", e.getMessage());
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        List<String> errors = new ArrayList<>();

        if ("createArticolo".equals(action)) {
            // creazione articolo
            String nome = request.getParameter("nome");
            String descrizione = request.getParameter("descrizione");
            Part imagePart = request.getPart("immagine");

            try (InputStream imageStream = (imagePart != null) ? imagePart.getInputStream() : null) {
                int prezzo = Integer.parseInt(request.getParameter("prezzo"));
                DbManager.inserisciArticolo(nome, descrizione, imageStream, prezzo, utente.getUsername());
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                errors.add("Errore nella creazione dell’articolo: " + e.getMessage());
            }

        } else if ("createAsta".equals(action)) {
            try {
                LocalDateTime scadenza = LocalDateTime.parse(request.getParameter("scadenza").replace(" ", "T"));
                if (scadenza.isBefore(LocalDateTime.now()))
                    throw new Exception("La data deve essere futura.");
                String[] articoliIds = request.getParameterValues("articoliId");
                List<Integer> idArticoli = new ArrayList<>();
                int prezzoIniziale = 0;

                for (String idStr : articoliIds) {
                    int id = Integer.parseInt(idStr);
                    idArticoli.add(id);
                    prezzoIniziale += DbManager.getPrezzoArticolo(id);
                }

                Part imagePart = request.getPart("immagine");
                String encodedImg = null;
                if (imagePart != null && imagePart.getSize() > 0) {
                    try (InputStream imageStream = imagePart.getInputStream()) {
                        byte[] imgBytes = imageStream.readAllBytes();
                        encodedImg = java.util.Base64.getEncoder().encodeToString(imgBytes);
                    }
                }

                Asta asta = new Asta();
                asta.setIdArticoli(idArticoli);
                asta.newOfferta(utente.getUsername(), prezzoIniziale);
                asta.setRialzoMinimo(Integer.parseInt(request.getParameter("rialzo")));
                asta.setScadenza(scadenza);
                asta.setNome(request.getParameter("nome"));
                asta.setDescrizione(request.getParameter("descrizione"));
                asta.setImmagine(encodedImg);

                DbManager.inserisciAsta(asta);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                errors.add("Errore nella creazione dell’asta: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write(new com.google.gson.Gson().toJson(Map.of("errors", errors)));
        }
    }
}
