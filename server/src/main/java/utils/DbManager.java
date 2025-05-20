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

    // Connection and transaction management methods
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        return conn;
    }

    private static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    private static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    private static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void inizializzaDatabase() {
        Connection con = null;

        try {
            con = getConnection();
            beginTransaction(con);

            Statement stmt = con.createStatement();
            // Load and execute schema from schema.sql file
            String schemaSql = new String(
                    Objects.requireNonNull(DbManager.class.getClassLoader().getResourceAsStream("schema.sql"))
                            .readAllBytes());

            for (String query : schemaSql.split(";")) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }

            // Insert "Alessio" user
            String insertUserSql = "INSERT INTO utente (username, pass_hash, nome, cognome, indirizzo) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username=username";
            PreparedStatement ps = con.prepareStatement(insertUserSql);
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

            commitTransaction(con);
            ps.close();
            stmt.close();

            System.out.println("Database inizializzato e utente inserito.");
        } catch (SQLException e) {
            rollbackTransaction(con);
            e.printStackTrace();
        } catch (Exception e) {
            rollbackTransaction(con);
            e.printStackTrace();
        } finally {
            closeConnection(con);
        }
    }

    public static void inserisciArticolo(String nome, String descrizione, InputStream imageStream, int prezzo, String venditore) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            beginTransaction(con);

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

            commitTransaction(con);
        } catch (java.io.IOException e) {
            rollbackTransaction(con);
            throw new SQLException("Error reading image stream", e);
        } catch (SQLException e) {
            rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static int getPrezzoArticolo(int idArticolo) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = getConnection();
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
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static void inserisciAsta(Asta a) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            beginTransaction(con);

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

            commitTransaction(con);
        } catch (SQLException e) {
            rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static List<Articolo> getArticoliDisponibiliPerUtente(String username) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Articolo> articoli = new ArrayList<>();

        try {
            con = getConnection();
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
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static List<Asta> getAsteUtente(String username, boolean chiuse) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = getConnection();
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
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setArticoli(getArticoliAsta(a.getId()));

                aste.add(a);
            }
            return aste;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static Asta getAstaById(int id) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Asta a = null;

        try {
            con = getConnection();
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
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setArticoli(getArticoliAsta(a.getId()));
            }
            return a;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static List<Asta> getAstePerParolaChiave(String parolaChiave, LocalDateTime now) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = getConnection();
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

                a.setArticoli(getArticoliAsta(a.getId()));
                a.setOfferte(getOffertePerAsta(a.getId()));

                aste.add(a);
            }
            return aste;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static List<Asta> getAsteVinteDaUtente(String username) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Asta> aste = new ArrayList<>();

        try {
            con = getConnection();
            String sql = "SELECT * FROM asta WHERE aggiudicatario = ? AND aggiudicatario IS NOT NULL";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

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
            return aste;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static Utente getUtente(String username) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Utente user = null;

        try {
            con = getConnection();
            String sql = "SELECT * FROM utente WHERE username = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new Utente();
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("pass_hash"));
                user.setNome(rs.getString("nome"));
                user.setCognome(rs.getString("cognome"));
                user.setIndirizzo(rs.getString("indirizzo"));
            }
            return user;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static void registraOfferta(int astaId, Offerta offerta) throws SQLException {
        registraOfferta(astaId, offerta.getUsername(), offerta.getPrezzo(), offerta.getDate());
    }

    public static void registraOfferta(int idAsta, String username, int prezzoOfferto, LocalDateTime date)
            throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            beginTransaction(con);

            String sql = "INSERT INTO offerta (id_asta, username, prezzo_offerto, data_offerta) VALUES (?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idAsta);
            ps.setString(2, username);
            ps.setInt(3, prezzoOfferto);
            ps.setString(4, date.toString());
            ps.executeUpdate();

            commitTransaction(con);
        } catch (SQLException e) {
            rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static List<Offerta> getOffertePerAsta(int idAsta) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Offerta> offerte = new ArrayList<>();

        try {
            con = getConnection();
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
            closeConnection(con);
        }
    }

    public static List<Articolo> getArticoliAsta(int idAsta) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Articolo> articoliAsta = new ArrayList<>();

        try {
            con = getConnection();
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
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }

    public static void chiudiAsta(int astaId, String aggiudicatario) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = getConnection();
            beginTransaction(con);

            String sql = "UPDATE asta SET aggiudicatario = ? WHERE id = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, aggiudicatario);
            ps.setInt(2, astaId);
            ps.executeUpdate();

            commitTransaction(con);
        } catch (SQLException e) {
            rollbackTransaction(con);
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(con);
        }
    }
}