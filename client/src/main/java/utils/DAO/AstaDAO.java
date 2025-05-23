package utils.DAO;

import model.Asta;
import model.Articolo;
import model.Asta.Offerta;

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

public class AstaDAO {

    public static void inserisciAsta(Asta a) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DbConnection.getConnection();
            DbConnection.beginTransaction(con);

            String sql = "INSERT INTO asta (nome, descrizione, immagine, scadenza, rialzo_minimo, venditore) VALUES (?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);

            ps.setString(1, a.getNome());
            ps.setString(2, a.getDescrizione());

            // Handle image as Base64-encoded string or null
            String img = a.getImmagine();
            if (img != null && !img.isEmpty()) {
                byte[] imgBytes = java.util.Base64.getDecoder().decode(img);
                ps.setBytes(3, imgBytes);
            } else {
                ps.setNull(3, java.sql.Types.BLOB);
            }

            ps.setString(4, a.getScadenza().toString());
            ps.setInt(5, a.getRialzoMinimo());
            ps.setString(6, a.getVenditore());
            ps.executeUpdate();

            // Retrieve generated auction ID
            int idAsta = -1;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT last_insert_rowid()");
            if (rs.next()) {
                idAsta = rs.getInt(1);
            }
            rs.close();
            st.close();

            // Insert associated articles and initial offer
            if (idAsta != -1) {
                for (Integer idArticolo : a.getIdArticoli()) {
                    String relSql = "INSERT INTO asta_articoli (id_asta, id_articolo) VALUES (?, ?)";
                    PreparedStatement psRel = con.prepareStatement(relSql);
                    psRel.setInt(1, idAsta);
                    psRel.setInt(2, idArticolo);
                    psRel.executeUpdate();
                    psRel.close();
                }

                // Register initial offer within the same transaction
                Offerta initialOffer = a.getOfferte().get(0);
                String offerSql = "INSERT INTO offerta (id_asta, username, prezzo_offerto, data_offerta) VALUES (?, ?, ?, ?)";
                PreparedStatement psOffer = con.prepareStatement(offerSql);
                psOffer.setInt(1, idAsta);
                psOffer.setString(2, initialOffer.getUsername());
                psOffer.setInt(3, initialOffer.getPrezzo());
                psOffer.setString(4, initialOffer.getDate().toString());
                psOffer.executeUpdate();
                psOffer.close();
            } else {
                throw new SQLException("Failed to retrieve generated auction ID");
            }

            DbConnection.commitTransaction(con);
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

    public static List<Asta> getAsteUtente(String username, boolean chiuse) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT * FROM asta WHERE venditore = ? AND (aggiudicatario IS "
                    + (chiuse ? "NOT NULL" : "NULL") + ")";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));

                // Handle image BLOB as Base64
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                a.setImmagine(encodedImg);
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(chiuse);

                // These method calls will use their own connections
                a.setOfferte(OffertaDAO.getOffertePerAsta(a.getId()));
                a.setArticoli(ArticoloDAO.getArticoliAsta(a.getId()));

                aste.add(a);
            }
            return aste;
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

    public static Asta getAstaById(int id) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Asta a = null;

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT * FROM asta WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));

                // Handle image BLOB as Base64
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                a.setImmagine(encodedImg);

                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setChiusa(rs.getString("aggiudicatario") != null);
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));

                // These method calls will use their own connections
                a.setOfferte(OffertaDAO.getOffertePerAsta(a.getId()));
                a.setArticoli(ArticoloDAO.getArticoliAsta(a.getId()));
            }
            return a;
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

    public static List<Asta> getAstePerParolaChiave(String parolaChiave, LocalDateTime now) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT * FROM asta " +
                    "WHERE (nome LIKE ? OR descrizione LIKE ?) " +
                    "AND aggiudicatario IS NULL " +
                    "AND scadenza > ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + parolaChiave + "%");
            ps.setString(2, "%" + parolaChiave + "%");
            ps.setObject(3, now.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));

                // Handle image BLOB as Base64
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                a.setImmagine(encodedImg);

                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(rs.getString("aggiudicatario") != null);

                a.setArticoli(ArticoloDAO.getArticoliAsta(a.getId()));
                a.setOfferte(OffertaDAO.getOffertePerAsta(a.getId()));

                aste.add(a);
            }
            return aste;
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

    public static List<Asta> getAsteVinteDaUtente(String username) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = DbConnection.getConnection();
            String sql = "SELECT * FROM asta WHERE aggiudicatario = ? AND aggiudicatario IS NOT NULL";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                List<Offerta> offerte = OffertaDAO.getOffertePerAsta(a.getId());
                if (offerte.size() <= 1)
                    continue;

                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));

                // Handle image BLOB as Base64
                byte[] imgData = rs.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                a.setImmagine(encodedImg);

                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(true);
                a.setArticoli(ArticoloDAO.getArticoliAsta(a.getId()));
                a.setOfferte(Arrays.asList(offerte.get(0), offerte.get(offerte.size() - 1)));
                aste.add(a);
            }
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
        return aste;
    }

    public static void chiudiAsta(int astaId, String aggiudicatario) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DbConnection.getConnection();
            DbConnection.beginTransaction(con);

            String sql = "UPDATE asta SET aggiudicatario = ? WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, aggiudicatario);
            ps.setInt(2, astaId);
            ps.executeUpdate();

            DbConnection.commitTransaction(con);
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

}