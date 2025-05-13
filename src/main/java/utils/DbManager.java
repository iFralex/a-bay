package utils;

import model.Articolo;
import model.Asta;
import model.Utente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DbManager {

    private static final String DB_URL = "jdbc:sqlite:aste.db"; // crea il file se non esiste

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void inizializzaDatabase() {
        try (Connection con = getConnection();
                Statement stmt = con.createStatement()) {

            String schemaSql = new String(
                    Objects.requireNonNull(DbManager.class.getClassLoader().getResourceAsStream("schema.sql"))
                            .readAllBytes());

            for (String query : schemaSql.split(";")) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }
            System.out.println("db init");
        } catch (SQLException e) {
            e.printStackTrace(); // o log
        } catch (Exception e) {
            e.printStackTrace(); // oppure stampa un messaggio custom
        }
    }

    public static void inserisciArticolo(Articolo a) throws SQLException {
        String sql = "INSERT INTO articolo (nome, descrizione, immagine, prezzo, venditore) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getNome());
            ps.setString(2, a.getDescrizione());
            ps.setString(3, a.getImmagine());
            ps.setDouble(4, a.getPrezzo());
            ps.setString(5, a.getVenditore());
            ps.executeUpdate();
        }
    }

    public static double getPrezzoArticolo(int idArticolo) {
        String query = "SELECT prezzo FROM articolo WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("prezzo");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void inserisciAsta(Asta a) {
        String sql = "INSERT INTO asta (nome, descrizione, immagine, scadenza, venditore) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getNome());
            ps.setString(2, a.getDescrizione());
            ps.setString(3, a.getImmagine());
            ps.setString(4, a.getScadenza().toString()); // SQLite salva DATETIME come TEXT
            ps.setString(5, a.getVenditore());
            ps.executeUpdate();

            // Recupera l'ID generato con last_insert_rowid()
            int idAsta = -1;
            try (Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    idAsta = rs.getInt(1);
                }
            }

            // Inserisci gli articoli associati all'asta
            if (idAsta != -1) {
                for (Integer idArticolo : a.getIdArticoli()) {
                    String relSql = "INSERT INTO asta_articoli (id_asta, id_articolo) VALUES (?, ?)";
                    try (PreparedStatement psRel = con.prepareStatement(relSql)) {
                        psRel.setInt(1, idAsta);
                        psRel.setInt(2, idArticolo);
                        psRel.executeUpdate();
                    }
                }
                registraOfferta(idAsta, a.getOfferte().get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Articolo> getArticoliDisponibiliPerUtente(String username) {
        List<Articolo> articoli = new ArrayList<>();
        String sql = "SELECT * FROM articolo WHERE venditore = ? AND id NOT IN (SELECT id_articolo FROM asta_articoli)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Articolo a = new Articolo();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));
                a.setImmagine(rs.getString("immagine"));
                a.setPrezzo(rs.getDouble("prezzo"));
                a.setVenditore(rs.getString("venditore"));
                articoli.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return articoli;
    }

    public static List<Asta> getAsteUtente(String username, boolean scadute) {
        List<Asta> aste = new ArrayList<>();
        String sql = "SELECT * FROM asta WHERE venditore = ? AND scadenza " + (scadute ? "<" : ">")
                + " datetime('now')";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));
                a.setImmagine(rs.getString("immagine"));
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setVenditore(rs.getString("venditore"));

                // Lista per articoli collegati all'asta
                List<Articolo> articoliAsta = new ArrayList<>();

                String sqlArticoli = "SELECT articolo.* FROM articolo " +
                        "JOIN asta_articoli ON articolo.id = asta_articoli.id_articolo " +
                        "WHERE asta_articoli.id_asta = ?";

                try (PreparedStatement psArt = con.prepareStatement(sqlArticoli)) {
                    psArt.setInt(1, a.getId());
                    ResultSet rsArt = psArt.executeQuery();
                    System.err.println("id " + a.getId());

                    while (rsArt.next()) {
                        Articolo art = new Articolo();
                        art.setId(rsArt.getInt("id"));
                        System.out.println("name " + rsArt.getString("nome"));
                        art.setNome(rsArt.getString("nome"));
                        art.setPrezzo(rsArt.getDouble("prezzo"));
                        // ... imposta altri campi se necessari
                        articoliAsta.add(art);
                    }
                }

                a.setArticoli(articoliAsta); // Supponendo che Asta abbia setArticoli(List<Articolo>)
                aste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aste;
    }

    public static Utente getUtente(String username) {
        if ("admin".equals(username)) { // confronta stringhe con equals()
            Utente user = new Utente();
            user.setNome("Alessio");
            user.setCognome("Antonucci");
            user.setIndirizzo("Via ciao, Mi");
            user.setUsername(username);
            return user;
        }
        return new Utente();
    }

    public static void registraOfferta(int astaId, Asta.Offerta offerta) throws SQLException {
        registraOfferta(astaId, offerta.getUsername(), offerta.getPrezzo(), offerta.getDate());
    }

    public static void registraOfferta(int idAsta, String username, double prezzoOfferto, LocalDateTime date)
            throws SQLException {
        String sql = "INSERT INTO offerta (id_asta, username, prezzo_offerto, data_offerta) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.setDouble(3, prezzoOfferto);
            ps.setString(4, date.toString());
            ps.executeUpdate();
        }
    }

    public static List<Asta.Offerta> getOffertePerAsta(int idAsta) throws SQLException {
        List<Asta.Offerta> offerte = new ArrayList<>();
        String sql = "SELECT username, prezzo_offerto, data_offerta FROM offerta WHERE id_asta = ? ORDER BY prezzo_offerto DESC";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String username = rs.getString("username");
                double prezzo = rs.getDouble("prezzo_offerto");
                LocalDateTime date = LocalDateTime.parse(rs.getString("data_offerta"));
                offerte.add(new Asta.Offerta(username, prezzo, date));
            }
        }

        return offerte;
    }
}
