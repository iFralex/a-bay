#!/bin/bash

# Crea la struttura delle directory
mkdir -p src/main/java/com/example/config
mkdir -p src/main/java/com/example/servlet
mkdir -p src/main/webapp/WEB-INF/templates

# Aggiorna il pom.xml
cat > pom.xml << 'EOL'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>thymeleaf-servlet-login</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <thymeleaf.version>3.1.2.RELEASE</thymeleaf.version>
    </properties>

    <dependencies>
        <!-- Thymeleaf -->
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf</artifactId>
            <version>${thymeleaf.version}</version>
        </dependency>
        
        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>thymeleaf-login</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
            </plugin>
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.2</version>
                <configuration>
                    <port>8080</port>
                    <path>/login-app</path>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOL

# Crea ThymeleafConfig.java
cat > src/main/java/com/example/config/ThymeleafConfig.java << 'EOL'
package com.example.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

/**
 * Configurazione di Thymeleaf per l'applicazione
 */
public class ThymeleafConfig {
    
    private static TemplateEngine templateEngine;
    
    /**
     * Inizializza il template engine di Thymeleaf
     * @param servletContext Il contesto della servlet
     * @return Il template engine configurato
     */
    public static TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if (templateEngine == null) {
            // Crea una nuova istanza del template engine
            templateEngine = new TemplateEngine();
            
            // Configura il resolver dei template
            ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
            
            // Configurazione base
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateResolver.setPrefix("/WEB-INF/templates/");
            templateResolver.setSuffix(".html");
            templateResolver.setCacheTTLMs(3600000L); // Cache per un'ora
            
            // In ambiente di sviluppo, disattiva la cache
            // templateResolver.setCacheable(false);
            
            // Aggiunge il resolver al template engine
            templateEngine.setTemplateResolver(templateResolver);
        }
        
        return templateEngine;
    }
}
EOL

# Crea LoginServlet.java
cat > src/main/java/com/example/servlet/LoginServlet.java << 'EOL'
package com.example.servlet;

import com.example.config.ThymeleafConfig;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet che gestisce la pagina di login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    
    @Override
    public void init() throws ServletException {
        // Inizializza il template engine Thymeleaf
        templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }
    
    /**
     * Gestisce la richiesta GET per mostrare la pagina di login
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Imposta la codifica della risposta
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Crea il contesto web per Thymeleaf
        WebContext context = new WebContext(
            request, 
            response, 
            getServletContext(), 
            request.getLocale()
        );
        
        // Processa il template e invia la risposta
        templateEngine.process("login", context, response.getWriter());
    }
    
    /**
     * Gestisce la richiesta POST per elaborare i dati di login
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Ottiene i parametri dalla richiesta
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Esempio semplice di autenticazione (da sostituire con logica reale)
        if ("admin".equals(username) && "password".equals(password)) {
            // Login riuscito: crea sessione
            HttpSession session = request.getSession(true);
            session.setAttribute("user", username);
            
            // Reindirizza alla pagina principale
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // Login fallito: mostra messaggio di errore
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            
            // Crea il contesto web per Thymeleaf
            WebContext context = new WebContext(
                request, 
                response, 
                getServletContext(), 
                request.getLocale()
            );
            
            // Aggiungi messaggio di errore al contesto
            context.setVariable("errorMessage", "Username o password non validi!");
            
            // Processa di nuovo il template di login con il messaggio di errore
            templateEngine.process("login", context, response.getWriter());
        }
    }
}
EOL

# Crea login.html
cat > src/main/webapp/WEB-INF/templates/login.html << 'EOL'
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        
        .login-container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            width: 350px;
        }
        
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 20px;
        }
        
        .form-group {
            margin-bottom: 15px;
        }
        
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
            font-size: 16px;
        }
        
        button {
            width: 100%;
            padding: 10px;
            background-color: #4285f4;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            font-weight: bold;
        }
        
        button:hover {
            background-color: #3367d6;
        }
        
        .error-message {
            color: #e53935;
            margin-top: 15px;
            font-size: 14px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>Accedi</h2>
        
        <!-- Mostra messaggio di errore se presente -->
        <div th:if="${errorMessage}" class="error-message" th:text="${errorMessage}"></div>
        
        <form th:action="@{/login}" method="post">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" id="username" name="username" required autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>
            </div>
            
            <button type="submit">Login</button>
        </form>
    </div>
</body>
</html>
EOL

# Crea web.xml
cat > src/main/webapp/WEB-INF/web.xml << 'EOL'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <display-name>Login Application</display-name>
    
    <!-- Welcome file list -->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
    
</web-app>
EOL

# Crea index.html
cat > src/main/webapp/index.html << 'EOL'
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Refresh" content="0; URL=login" />
</head>
<body>
    <p>Redirecting to login page...</p>
</body>
</html>
EOL

echo "Configurazione del progetto completata con successo!"
echo "Per eseguire il progetto, usa il comando: mvn tomcat7:run"
echo "Poi visita http://localhost:8080/login-app nel tuo browser"