package it.polimi.demo;

import java.sql.*;

/**
 * Simple utility class to test MySQL database connections.
 */
public class ConnectionTester {
    public static void main(String[] args){
        final String DATABASE = "";
        final String USER = "root";
        final String PASSWORD = "root";
        Connection connection = null;

        // Load the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found");
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + DATABASE, USER, PASSWORD);
            System.out.println("Database connection successful");

            // Print connection details
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("Connected to: " + metaData.getDatabaseProductName() + " " +
                    metaData.getDatabaseProductVersion());
            System.out.println("Using driver: " + metaData.getDriverName() + " " +
                    metaData.getDriverVersion());

            connection.close();
        } catch (Exception e) {
            System.err.println("Connection failed");
            e.printStackTrace();
        }
    }
}
