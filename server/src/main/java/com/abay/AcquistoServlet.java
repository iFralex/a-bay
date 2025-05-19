package com.abay;

import model.Asta;
import model.Asta.Offerta;
import model.Utente;
import utils.DbManager;;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/acquisto")
public class AcquistoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String parolaChiave = request.getParameter("parolaChiave");
        LocalDateTime now = LocalDateTime.now();

        List<String> errors = new ArrayList<>();

        // Recupera le aste aperte che corrispondono alla parola chiave
        List<Asta> asteAperte = new ArrayList<>();
        if (parolaChiave != null && !parolaChiave.trim().isEmpty()) {
            try {
                asteAperte = DbManager.getAstePerParolaChiave(parolaChiave, now);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            } catch (SQLException e) {
                errors.add("Errore nel database: " + e.getMessage());
            }
        }

        // Ordina le aste per tempo rimanente decrescente
        asteAperte.sort(Comparator.comparing(
                (Asta a) -> java.time.Duration.between(now, a.getScadenza()))
                .reversed());

        // Recupera le offerte aggiudicate all'utente (se loggato)
        List<Asta> asteAggiudicate = new ArrayList<>();
        HttpSession session = request.getSession(false);
        if (session != null) {
            Utente utente = (Utente) session.getAttribute("user");
            if (utente != null) {
                try {
                    asteAggiudicate = DbManager.getAsteVinteDaUtente(utente.getUsername());
                } catch (IllegalArgumentException e) {
                    errors.add(e.getMessage());
                } catch (SQLException e) {
                    errors.add("Errore nel database: " + e.getMessage());
                }
            }
        }

        request.setAttribute("asteAperte", asteAperte);
        request.setAttribute("asteAggiudicate", asteAggiudicate);
        request.setAttribute("parolaChiave", parolaChiave);
        if (errors.size() > 0)
            request.setAttribute("errors", errors);

        request.getRequestDispatcher("/acquisto.jsp").forward(request, response);
    }
}