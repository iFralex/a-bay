package utils.DAO;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public class DbConnection {
    private static final String DB_URL;

    static {
        Properties props = new Properties();
        try (InputStream input = DbConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(input);
            String path = props.getProperty("db.path");
            DB_URL = "jdbc:sqlite:" + path;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC Driver not found", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace(); // Log the error, do not throw
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace(); // Log the error, do not throw
            }
        }
    }

    public static void inizializzaDatabase() {
        Connection con = null;
        Statement stmt = null;
        PreparedStatement ps = null;

        try {
            con = DbConnection.getConnection();
            DbConnection.beginTransaction(con);

            stmt = con.createStatement();
            String schemaSql = new String(
                    Objects.requireNonNull(DbConnection.class.getClassLoader().getResourceAsStream("schema.sql"))
                            .readAllBytes());

            for (String query : schemaSql.split(";")) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }

            String insertUserSql = "INSERT INTO utente (username, pass_hash, nome, cognome, indirizzo) VALUES (?, ?, ?, ?, ?) ON CONFLICT(username) DO NOTHING"; // Usare ON CONFLICT per SQLite
            ps = con.prepareStatement(insertUserSql);

            ps.setString(1, "admin"); // username
            ps.setString(2, "$2a$12$dyQptPkpjK5a1R5prGq1uulyUYdhF2rBoQODWPHlNWLMUvdbqsg02"); // password: "admin"
            ps.setString(3, "Alessio");
            ps.setString(4, "Antonucci");
            ps.setString(5, "Via ciao, Mi");
            ps.executeUpdate();

            ps.setString(1, "user2"); // username
            ps.setString(2, "$2a$12$dyQptPkpjK5a1R5prGq1uulyUYdhF2rBoQODWPHlNWLMUvdbqsg02"); // password: "admin"
            ps.setString(3, "Ale2");
            ps.setString(4, "Anto2");
            ps.setString(5, "Via ciao 2, Mi");
            ps.executeUpdate();

            DbConnection.commitTransaction(con);
            System.out.println("Database inizializzato e utenti inseriti.");
        } catch (SQLException | java.io.IOException | NullPointerException e) {
            DbConnection.rollbackTransaction(con);
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DbConnection.closeConnection(con);
        }
    }
}