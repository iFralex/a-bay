package com.example;

import model.Asta;
import model.Utente;
import utils.DbManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/creaAsta")
public class CreaAstaServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;
        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String[] articoliIds = request.getParameterValues("articoliId");
        int rialzoMinimo = Integer.parseInt(request.getParameter("rialzo"));
        String scadenzaString = request.getParameter("scadenza");
        String nomeString = request.getParameter("nome");
        String descrizioneString = request.getParameter("descrizione");
        String immagineString = request.getParameter("immagine");

        List<Integer> idArticoli = new ArrayList<>();
        double prezzoIniziale = 0;

        for (String idStr : articoliIds) {
            int id = Integer.parseInt(idStr);
            idArticoli.add(id);
            prezzoIniziale += DbManager.getPrezzoArticolo(id); // somma dei prezzi
        }

        Asta asta = new Asta();
        asta.setIdArticoli(idArticoli);
        asta.newOfferta(utente.getUsername(), prezzoIniziale);
        asta.setRialzoMinimo(rialzoMinimo);
        asta.setScadenza(LocalDateTime.parse(scadenzaString.replace(" ", "T")));
        asta.setNome(nomeString);
        asta.setDescrizione(descrizioneString);
        asta.setImmagine(immagineString);

        DbManager.inserisciAsta(asta);

        response.sendRedirect("vendo");
    }
}
