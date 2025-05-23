package utils.DAO;

import model.Asta;
import model.Articolo;
import model.Offerta;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ArticoloDAO {
    
    public static void inserisciArticolo(String nome, String descrizione, InputStream imageStream, int prezzo, String venditore) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            Articolo art = new Articolo(-1, nome, prezzo, venditore, descrizione);
            con = DbConnection.getConnection();
            DbConnection.beginTransaction(con);

            String sql = "INSERT INTO articolo (nome, descrizione, immagine, prezzo, venditore) VALUES (?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, nome);
            ps.setString(2, descrizione);

            // Read InputStream to byte[]
            byte[] imageBytes = null;
            if (imageStream != null) {
                imageBytes = imageStream.readAllBytes();
            }
            if (imageBytes != null) {
                ps.setBytes(3, imageBytes);
            } else {
                ps.setNull(3, java.sql.Types.BLOB);
            }

            ps.setInt(4, prezzo);
            ps.setString(5, venditore);
            ps.executeUpdate();

            DbConnection.commitTransaction(con);
        } catch (java.io.IOException e) {
            DbConnection.rollbackTransaction(con);
            throw new SQLException("Error reading image stream", e);
        } catch (SQLException e) {
            DbConnection.rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }

    public static int getPrezzoArticolo(int idArticolo) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DbConnection.getConnection();
            String query = "SELECT prezzo FROM articolo WHERE id = ?";
            ps = con.prepareStatement(query);
            ps.setInt(1, idArticolo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("prezzo");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }

    public static List<Articolo> getArticoliDisponibiliPerUtente(String username) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Articolo> articoli = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT * FROM articolo WHERE venditore = ? AND id NOT IN (SELECT id_articolo FROM asta_articoli)";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            while (rs.next()) {
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = Base64.getEncoder().encodeToString(imgData);
                }
                Articolo a = new Articolo(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("prezzo"),
                        rs.getString("venditore"),
                        rs.getString("descrizione"),
                        encodedImg);
                articoli.add(a);
            }
            return articoli;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }

    public static List<Articolo> getArticoliAsta(int idAsta) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Articolo> articoliAsta = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sqlArticoli = "SELECT articolo.* FROM articolo " +
                    "JOIN asta_articoli ON articolo.id = asta_articoli.id_articolo " +
                    "WHERE asta_articoli.id_asta = ?";
            ps = con.prepareStatement(sqlArticoli);
            ps.setInt(1, idAsta);
            rs = ps.executeQuery();

            while (rs.next()) {
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                Articolo art = new Articolo(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("prezzo"),
                        rs.getString("venditore"),
                        rs.getString("descrizione"),
                        encodedImg);
                articoliAsta.add(art);
            }
            return articoliAsta;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException e) {
                    /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }
}