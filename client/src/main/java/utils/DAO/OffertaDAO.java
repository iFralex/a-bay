package utils.DAO;

import model.Asta;
import model.Asta.Offerta;
import model.Articolo;

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

public class OffertaDAO {

    public static void registraOfferta(int astaId, Offerta offerta) throws SQLException {
        registraOfferta(astaId, offerta.getUsername(), offerta.getPrezzo(), offerta.getDate());
    }

    public static void registraOfferta(int idAsta, String username, int prezzoOfferto, LocalDateTime date)
            throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DbConnection.getConnection();
            DbConnection.beginTransaction(con);

            String sql = "INSERT INTO offerta (id_asta, username, prezzo_offerto, data_offerta) VALUES (?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.setInt(3, prezzoOfferto);
            ps.setString(4, date.toString());
            ps.executeUpdate();

            DbConnection.commitTransaction(con);
        } catch (SQLException e) {
            DbConnection.rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }

    public static List<Offerta> getOffertePerAsta(int idAsta) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Offerta> offerte = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT username, prezzo_offerto, data_offerta FROM offerta WHERE id_asta = ? ORDER BY prezzo_offerto";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idAsta);
            rs = ps.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                int prezzo = rs.getInt("prezzo_offerto");
                LocalDateTime date = LocalDateTime.parse(rs.getString("data_offerta"));
                Offerta offerta = new Offerta();
                offerta.setUsername(username);
                offerta.setPrezzo(prezzo);
                offerta.setDate(date);
                offerte.add(offerta);
            }
            return offerte;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            DbConnection.closeConnection(con);
        }
    }
}