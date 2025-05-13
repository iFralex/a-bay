package com.example;

import model.Articolo;
import model.Utente;
import utils.DbManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/creaArticolo")
public class CreaArticoloServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");
        String immagine = request.getParameter("immagine");
        double prezzo = Double.parseDouble(request.getParameter("prezzo"));

        Articolo articolo = new Articolo();
        articolo.setNome(nome);
        articolo.setDescrizione(descrizione);
        articolo.setImmagine(immagine);
        articolo.setPrezzo(prezzo);
        articolo.setVenditore(utente.getUsername());

        try {
            DbManager.inserisciArticolo(articolo);
        } catch (SQLException e) {
            e.printStackTrace(); // oppure gestisci meglio l’errore
        }

        response.sendRedirect("vendo");
    }
}
