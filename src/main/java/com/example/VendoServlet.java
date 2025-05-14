package com.example;

import model.*;
import utils.DbManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/vendo")
public class VendoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Utente utente = (session != null) ? (Utente) session.getAttribute("user") : null;

        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        List<Articolo> articoliUtente = DbManager.getArticoliDisponibiliPerUtente(utente.getUsername());
        for (Articolo art : articoliUtente) {
            System.out.println(art.getNome());
        }

        List<Asta> asteAperte = DbManager.getAsteUtente(utente.getUsername(), false); // non chiuse
        List<Asta> asteChiuse = DbManager.getAsteUtente(utente.getUsername(), true); // chiuse

        request.setAttribute("articoliUtente", articoliUtente);
        request.setAttribute("asteAperte", asteAperte);
        request.setAttribute("asteChiuse", asteChiuse);

        request.getRequestDispatcher("/vendo.jsp").forward(request, response);
    }


    //succede se CreaAstaServlet vede che l'asta e' vuota
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Non gestiamo POST in questo servlet
        this.doGet(request, response);
    }
}
