package com.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Establecer un atributo para usar con JSTL
        String message = "¡Hola Mundo desde Servlet!";
        request.setAttribute("message", message);
        
        // Crear un array para demostrar el uso de JSTL
        String[] names = {"Java", "Servlet", "JSP", "JSTL"};
        request.setAttribute("technologies", names);
        
        // Redirigir al JSP
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}