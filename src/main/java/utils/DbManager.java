package utils;

import model.Articolo;
import model.Asta;
import model.Asta.Offerta;
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
            e.printStackTrace();
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
            String insertUserSql = "INSERT OR IGNORE INTO utente (username, pass_hash, nome, cognome, indirizzo) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insertUserSql)) {
                ps.setString(1, "admin"); // username
                ps.setString(2, "$2a$12$dyQptPkpjK5a1R5prGq1uulyUYdhF2rBoQODWPHlNWLMUvdbqsg02"); // password: "admin"
                ps.setString(3, "Alessio");
                ps.setString(4, "Antonucci");
                ps.setString(5, "Via ciao, Mi");
                ps.executeUpdate();
            }

            System.out.println("Database inizializzato e utente inserito.");
        } catch (SQLException e) {
            e.printStackTrace(); // o log
        } catch (Exception e) {
            e.printStackTrace();
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
        String sql = "INSERT INTO asta (nome, descrizione, immagine, scadenza, rialzo_minimo, venditore) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getNome());
            ps.setString(2, a.getDescrizione());
            ps.setString(3, a.getImmagine());
            ps.setString(4, a.getScadenza().toString()); // SQLite salva DATETIME come TEXT
            ps.setInt(5, a.getRialzoMinimo());
            ps.setString(6, a.getVenditore());
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

    public static List<Asta> getAsteUtente(String username, boolean chiuse) {
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
                a.setImmagine(rs.getString("immagine"));
                a.setOfferte(getOffertePerAsta(a.getId()));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(chiuse);

                a.setArticoli(getArticoliAsta(a.getId()));
                aste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aste;
    }

    public static Asta getAstaById(int id) {
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
                a.setImmagine(rs.getString("immagine"));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setChiusa(rs.getString("aggiudicatario") != null);

                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));

                // Recupera le offerte associate all'asta
                a.setOfferte(getOffertePerAsta(a.getId()));

                a.setArticoli(getArticoliAsta(a.getId()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return a;
    }

    public static List<Asta> getAstePerParolaChiave(String parolaChiave, LocalDateTime now) {
        List<Asta> aste = new ArrayList<>();
        String sql = "SELECT * FROM asta " +
                "WHERE (nome LIKE ? OR descrizione LIKE ?) " +
                "AND aggiudicatario IS NULL " + // Seleziona le aste senza aggiudicatario (aperte)
                "AND scadenza > ?"; // Aste con scadenza futura

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + parolaChiave + "%"); // Ricerca nel nome
            ps.setString(2, "%" + parolaChiave + "%"); // Ricerca nella descrizione
            ps.setObject(3, now); // La data e ora attuali per confrontare la scadenza

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));
                a.setImmagine(rs.getString("immagine"));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(rs.getString("aggiudicatario") != null);

                // Recupera gli articoli e le offerte per l'asta
                a.setArticoli(getArticoliAsta(a.getId()));
                a.setOfferte(getOffertePerAsta(a.getId()));

                aste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aste;
    }

    public static List<Asta> getAsteVinteDaUtente(String username) {
        List<Asta> aste = new ArrayList<>();
        String sql = "SELECT * FROM asta WHERE aggiudicatario = ? AND aggiudicatario IS NOT NULL";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username); // Imposta il parametro username nella query
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Asta a = new Asta();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setDescrizione(rs.getString("descrizione"));
                a.setImmagine(rs.getString("immagine"));
                a.setScadenza(LocalDateTime.parse(rs.getString("scadenza")));
                a.setRialzoMinimo(rs.getInt("rialzo_minimo"));
                a.setChiusa(true); // L'asta è chiusa (vinta)
                a.setArticoli(getArticoliAsta(a.getId())); // Ottieni gli articoli per l'asta

                List<Offerta> offerte = getOffertePerAsta(a.getId());
                a.setOfferte(Arrays.asList(offerte.get(0), offerte.get(offerte.size() - 1))); // Imposta prima e ultima

                aste.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return aste;
    }

    public static Utente getUtente(String username) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user != null ? user : new Utente(); // se non trovato, ritorna oggetto vuoto
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

    public static List<Articolo> getArticoliAsta(int idAsta) {
        List<Articolo> articoliAsta = new ArrayList<>();
        String sqlArticoli = "SELECT articolo.* FROM articolo " +
                "JOIN asta_articoli ON articolo.id = asta_articoli.id_articolo " +
                "WHERE asta_articoli.id_asta = ?";
        try (Connection con = getConnection(); PreparedStatement psArt = con.prepareStatement(sqlArticoli)) {
            psArt.setInt(1, idAsta);
            ResultSet rsArt = psArt.executeQuery();

            while (rsArt.next()) {
                Articolo art = new Articolo();
                art.setId(rsArt.getInt("id"));
                art.setNome(rsArt.getString("nome"));
                art.setPrezzo(rsArt.getDouble("prezzo"));
                // imposta altri campi se necessari
                articoliAsta.add(art);
            }
            return articoliAsta;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void chiudiAsta(int astaId, String aggiudicatario) {
        String sql = "UPDATE asta SET aggiudicatario = ? WHERE id = ?";

        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, astaId);
            ps.setString(2, aggiudicatario);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
