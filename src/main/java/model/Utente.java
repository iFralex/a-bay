package model;

public class Utente {
    private String username;
    private String passwordHash;
    private String nome;
    private String cognome;
    private String indirizzo;

    // Setters
    public void setUsername(String username) {
        if (username == null || username.trim().length() < 1 || username.trim().length() >= 100) {
            throw new IllegalArgumentException("Username non valido: deve avere almeno 1 carattere o maggiore di 100 caratteri.");
        }
        this.username = username.trim();
    }

    public void setPasswordHash(String hash) {
        if (hash == null || hash.trim().length() < 10 || hash.trim().length() >= 250) {
            throw new IllegalArgumentException("Hash della password non valido: deve avere almeno 10 caratteri e al più 250.");
        }
        this.passwordHash = hash.trim();
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().length() < 2 || nome.trim().length() >= 100) {
            throw new IllegalArgumentException("Nome non valido: deve avere almeno 2 caratteri e al più 100.");
        }
        this.nome = nome.trim();
    }

    public void setCognome(String cognome) {
        if (cognome == null || cognome.trim().length() < 2 || cognome.trim().length() >= 100) {
            throw new IllegalArgumentException("Cognome non valido: deve avere almeno 2 caratteri e al più 100.");
        }
        this.cognome = cognome.trim();
    }

    public void setIndirizzo(String indirizzo) {
        if (indirizzo == null || indirizzo.trim().length() < 6 || indirizzo.trim().length() >= 400) {
            throw new IllegalArgumentException("Indirizzo non valido: non valido: deve avere almeno 6 caratteri e al più 400.");
        }
        this.indirizzo = indirizzo.trim();
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getIndirizzo() {
        return indirizzo;
    }
}
