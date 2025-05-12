package it.polimi.demo;

import java.sql.*;

/**
 * Simple utility class to test MySQL database connections and perform database operations.
 */
public class ConnectionTester {
    public static void main(String[] args){
        final String DATABASE = "TIWdb";
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

            // Insert a new user
            String insertUserSQL = "INSERT INTO `user` (username, password, name, address) " +
                    "VALUES (?, ?, ?, ?)";

            // Set user details
            String username = "john_doe";
            String password = "securePassword123";
            String name = "John Doe";
            String address = "123 Main Street, Milano, Italy";

            try (PreparedStatement pstmt = connection.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, address);

                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " user record inserted.");

                // Get the auto-generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        System.out.println("New user ID: " + userId);

                        // Now retrieve and display the user
                        String selectUserSQL = "SELECT * FROM `user` WHERE id = ?";
                        try (PreparedStatement selectStmt = connection.prepareStatement(selectUserSQL)) {
                            selectStmt.setInt(1, userId);

                            try (ResultSet rs = selectStmt.executeQuery()) {
                                System.out.println("\nUser Details:");
                                System.out.println("--------------------------------------------");
                                System.out.printf("%-5s | %-15s | %-15s | %-25s | %-30s\n",
                                        "ID", "Username", "Password", "Name", "Address");
                                System.out.println("--------------------------------------------");

                                if (rs.next()) {
                                    System.out.printf("%-5d | %-15s | %-15s | %-25s | %-30s\n",
                                            rs.getInt("id"),
                                            rs.getString("username"),
                                            "********", // Hide actual password for security
                                            rs.getString("name"),
                                            rs.getString("address"));
                                }
                            }
                        }
                    }
                }
            }

            connection.close();
        } catch (SQLException e) {
            // Handle specific SQL exceptions
            if (e.getErrorCode() == 1062) {
                System.err.println("Error: Username already exists. Please choose a different username.");
            } else {
                System.err.println("SQL Error: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Connection failed");
            e.printStackTrace();
        }
    }
}