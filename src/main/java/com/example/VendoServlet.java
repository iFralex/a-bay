package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.*;
import utils.DbManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/vendo")
@MultipartConfig
public class VendoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

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

        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        List<String> errors = new ArrayList<>();

        try {
            try {
                List<Articolo> articoliUtente = DbManager.getArticoliDisponibiliPerUtente(utente.getUsername());
                request.setAttribute("articoliUtente", articoliUtente);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }

            try {
                List<Asta> asteAperte = DbManager.getAsteUtente(utente.getUsername(), false);
                request.setAttribute("asteAperte", asteAperte);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }

            try {
                List<Asta> asteChiuse = DbManager.getAsteUtente(utente.getUsername(), true);
                request.setAttribute("asteChiuse", asteChiuse);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        } catch (SQLException e) {
            errors.add("Errore nel Database:" + e.getMessage());
        }

        Object attr = request.getAttribute("errors");
        if (attr instanceof List)
            errors.addAll((List<String>) attr);

        if (!errors.isEmpty())
            request.setAttribute("errors", errors);

        request.getRequestDispatcher("/vendo.jsp").forward(request, response);
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

        System.out.println("Parameters:");
        request.getParameterMap().forEach((k, v) -> System.out.println(k + " = " + Arrays.toString(v)));

        String action = request.getParameter("action");
        List<String> errors = new ArrayList<>();

        if ("createArticolo".equals(action)) {
            // Creazione articolo
            String nome = request.getParameter("nome");
            String descrizione = request.getParameter("descrizione");
            Part imagePart = request.getPart("immagine");
            InputStream imageStream = null;
            String mimeType = null;
            if (imagePart != null) {
                imageStream = imagePart.getInputStream();
                String filename = imagePart.getSubmittedFileName();
                mimeType = getServletContext().getMimeType(filename);
            }

            try {
                int prezzo = Integer.parseInt(request.getParameter("prezzo"));
                DbManager.inserisciArticolo(nome, descrizione, imageStream, prezzo, utente.getUsername());
                session.setAttribute("success", "Articolo salvato con successo!");
            } catch (NumberFormatException e) {
                errors.add("Prezzo non valido.");
            } catch (IllegalArgumentException e) {
                errors.add("Impossibile aggiungere l'articolo: " + e.getMessage());
            } catch (SQLException e) {
                errors.add("Errore nel database durante l'inserimento dell'articolo.");
                e.printStackTrace();
            }

        } else if ("createAsta".equals(action)) {
            // Auction creation with image upload
            String[] articoliIds = request.getParameterValues("articoliId");
            if (articoliIds == null)
                articoliIds = new String[0];

            try {
                List<Integer> idArticoli = new ArrayList<>();
                int prezzoIniziale = 0;

                for (String idStr : articoliIds) {
                    int id = Integer.parseInt(idStr);
                    idArticoli.add(id);
                    prezzoIniziale += DbManager.getPrezzoArticolo(id);
                }

                // Handle image upload
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
                asta.setScadenza(LocalDateTime.parse(request.getParameter("scadenza").replace(" ", "T")));
                asta.setNome(request.getParameter("nome"));
                asta.setDescrizione(request.getParameter("descrizione"));
                asta.setImmagine(encodedImg);

                DbManager.inserisciAsta(asta);
                session.setAttribute("success", "Asta salvata con successo!");
            } catch (IllegalArgumentException e) {
                errors.add("Impossibile creare l'asta: " + e.getMessage());
            } catch (Exception e) {
                errors.add("Errore nella creazione dell'asta. Verifica i dati inseriti.");
                e.printStackTrace();
            }
        }

        if (!errors.isEmpty()) {
            session.setAttribute("errors", errors);
        }

        // Reindirizza alla vista con i dati aggiornati (POST-Redirect-GET)
        response.sendRedirect("vendo");
    }
}
