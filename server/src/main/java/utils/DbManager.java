package utils;

import model.Articolo;
import model.Asta;
import model.Offerta;
import model.Utente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.InputStream;

public class DbManager {
    private static final String DB_URL;

    static {
        Properties props = new Properties();
        try (InputStream input = DbManager.class.getClassLoader().getResourceAsStream("config.properties")) {
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
            throw new RuntimeException(e.getMessage());
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void inizializzaDatabase() {
        try (Connection con = getConnection();
                Statement stmt = con.createStatement()) {

            // Carica ed esegui lo schema dal file schema.sql
            String schemaSql = new String(
                    Objects.requireNonNull(DbManager.class.getClassLoader().getResourceAsStream("schema.sql"))
                            .readAllBytes());

            for (String query : schemaSql.split(";")) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }

            // Inserimento utente "Alessio"
            String insertUserSql = "INSERT INTO utente (username, pass_hash, nome, cognome, indirizzo) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username=username";
            try (PreparedStatement ps = con.prepareStatement(insertUserSql)) {
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
            }

            System.out.println("Database inizializzato e utente inserito.");
        } catch (SQLException e) {
            e.printStackTrace(); // o log
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void inserisciArticolo(String nome, String descrizione, InputStream imageStream, int prezzo, String venditore) throws SQLException {
        String sql = "INSERT INTO articolo (nome, descrizione, immagine, prezzo, venditore) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (java.io.IOException e) {
            throw new SQLException("Error reading image stream", e);
        }
    }

    public static int getPrezzoArticolo(int idArticolo) {
        String query = "SELECT prezzo FROM articolo WHERE id = ?";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idArticolo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("prezzo");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void inserisciAsta(Asta a) throws SQLException {
        String sql = "INSERT INTO asta (nome, descrizione, immagine, scadenza, rialzo_minimo, venditore) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    idAsta = rs.getInt(1);
                }
            }

            // Insert associated articles and initial offer
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
        }
    }

    public static List<Articolo> getArticoliDisponibiliPerUtente(String username) throws SQLException {
        List<Articolo> articoli = new ArrayList<>();
        String sql = "SELECT * FROM articolo WHERE venditore = ? AND id NOT IN (SELECT id_articolo FROM asta_articoli)";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
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
        }
        //
        return articoli;
    }

    public static List<Asta> getAsteUtente(String username, boolean chiuse) throws SQLException {
        List<Asta> aste = new ArrayList<>();

        String sql = "SELECT * FROM asta WHERE venditore = ? AND (aggiudicatario IS "
                + (chiuse ? "NOT NULL" : "NULL") + ")";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

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
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(chiuse);

                a.setArticoli(getArticoliAsta(a.getId()));
                aste.add(a);
            }
        }

        return aste;
    }

    public static Asta getAstaById(int id) throws SQLException {
        Asta a = null;
        String sql = "SELECT * FROM asta WHERE id = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

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

                // Retrieve associated offers and articles
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setArticoli(getArticoliAsta(a.getId()));
            }
        }

        return a;
    }

    public static List<Asta> getAstePerParolaChiave(String parolaChiave, LocalDateTime now) throws SQLException {
        List<Asta> aste = new ArrayList<>();
        String sql = "SELECT * FROM asta " +
                "WHERE (nome LIKE ? OR descrizione LIKE ?) " +
                "AND aggiudicatario IS NULL " +
                "AND scadenza > ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + parolaChiave + "%");
            ps.setString(2, "%" + parolaChiave + "%");
            ps.setObject(3, now);

            ResultSet rs = ps.executeQuery();

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

                a.setArticoli(getArticoliAsta(a.getId()));
                a.setOfferte(getOffertePerAsta(a.getId()));

                aste.add(a);
            }
        }
        return aste;
    }

    public static List<Asta> getAsteVinteDaUtente(String username) {
        List<Asta> aste = new ArrayList<>();
        String sql = "SELECT * FROM asta WHERE aggiudicatario = ? AND aggiudicatario IS NOT NULL";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                List<Offerta> offerte = getOffertePerAsta(a.getId());
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
                a.setArticoli(getArticoliAsta(a.getId()));
                a.setOfferte(Arrays.asList(offerte.get(0), offerte.get(offerte.size() - 1)));
                aste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aste;
    }

    public static Utente getUtente(String username) throws SQLException  {
        Utente user = null;
        String sql = "SELECT * FROM utente WHERE username = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new Utente();
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("pass_hash"));
                user.setNome(rs.getString("nome"));
                user.setCognome(rs.getString("cognome"));
                user.setIndirizzo(rs.getString("indirizzo"));
            }
        }

        return user;
    }

    public static void registraOfferta(int astaId, Offerta offerta) throws SQLException {
        registraOfferta(astaId, offerta.getUsername(), offerta.getPrezzo(), offerta.getDate());
    }

    public static void registraOfferta(int idAsta, String username, int prezzoOfferto, LocalDateTime date)
            throws SQLException {
        String sql = "INSERT INTO offerta (id_asta, username, prezzo_offerto, data_offerta) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.setInt(3, prezzoOfferto);
            ps.setString(4, date.toString());
            ps.executeUpdate();
        }
    }

    public static List<Offerta> getOffertePerAsta(int idAsta) throws SQLException {
        List<Offerta> offerte = new ArrayList<>();
        String sql = "SELECT username, prezzo_offerto, data_offerta FROM offerta WHERE id_asta = ? ORDER BY prezzo_offerto";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsta);
            ResultSet rs = ps.executeQuery();
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
        }

        return offerte;
    }

    public static List<Articolo> getArticoliAsta(int idAsta) throws SQLException {
        List<Articolo> articoliAsta = new ArrayList<>();
        String sqlArticoli = "SELECT articolo.* FROM articolo " +
                "JOIN asta_articoli ON articolo.id = asta_articoli.id_articolo " +
                "WHERE asta_articoli.id_asta = ?";
        try (Connection con = getConnection(); PreparedStatement psArt = con.prepareStatement(sqlArticoli)) {
            psArt.setInt(1, idAsta);
            ResultSet rsArt = psArt.executeQuery();

            while (rsArt.next()) {
                byte[] imgData = rsArt.getBytes("immagine");
                String encodedImg = null;
                if (imgData != null && imgData.length > 0) {
                    encodedImg = java.util.Base64.getEncoder().encodeToString(imgData);
                }
                Articolo art = new Articolo(
                        rsArt.getInt("id"),
                        rsArt.getString("nome"),
                        rsArt.getInt("prezzo"),
                        rsArt.getString("venditore"),
                        rsArt.getString("descrizione"),
                        encodedImg);
                articoliAsta.add(art);
            }
            return articoliAsta;
        }
    }

    public static void chiudiAsta(int astaId, String aggiudicatario) throws SQLException {
        String sql = "UPDATE asta SET aggiudicatario = ? WHERE id = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, aggiudicatario);
            ps.setInt(2, astaId);
            ps.executeUpdate();
        }
    }
}