package com.example;

import model.Asta;
import model.Asta.Offerta;
import model.Utente;
import utils.DbManager;;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/acquisto")
public class AcquistoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String parolaChiave = request.getParameter("parolaChiave");
        LocalDateTime now = LocalDateTime.now();

        // Recupera le aste aperte che corrispondono alla parola chiave
        List<Asta> asteAperte = new ArrayList<>();
        if (parolaChiave != null && !parolaChiave.trim().isEmpty()) {
            asteAperte = DbManager.getAstePerParolaChiave(parolaChiave, now);
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
                asteAggiudicate = DbManager.getAsteVinteDaUtente(utente.getUsername());
            }
        }

        request.setAttribute("asteAperte", asteAperte);
        request.setAttribute("asteAggiudicate", asteAggiudicate);
        request.setAttribute("parolaChiave", parolaChiave);

        request.getRequestDispatcher("/acquisto.jsp").forward(request, response);
    }
}