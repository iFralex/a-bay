package model;

public class Articolo {
    private int id;
    private String nome;
    private String descrizione;
    private String immagine;
    private int prezzo;
    private String venditore; // username

    public Articolo(int id, String nome, int prezzo, String venditore, String descrizione, String immagine) {
        setId(id);
        setNome(nome);
        setDescrizione(descrizione);
        setImmagine(immagine);
        setPrezzo(prezzo);
        setVenditore(venditore);
    }

    public Articolo(int id, String nome, int prezzo, String venditore, String descrizione) {
        this(id, nome, prezzo, venditore, descrizione, "");
    }

    public Articolo(int id, String nome, int prezzo, String venditore) {
        this(id, nome, prezzo, venditore, "", "");
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().length() < 3 || nome.trim().length() >= 200) {
            throw new IllegalArgumentException("Nome articolo non valido: deve avere almeno 3 caratteri e al più 200.");
        }
        this.nome = nome.trim();
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        if (descrizione != null && descrizione.trim().length() >= 1000) {
            throw new IllegalArgumentException("descrizione non valido: non può essere più lungo di 1000 caratteri.");
        }
        this.descrizione = descrizione.trim();
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public int getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(int prezzo) {
        if (prezzo <= 0 || prezzo >= 500000) {
            throw new IllegalArgumentException("Prezzo non valido: deve essere maggiore di zero e minore di 500000.");
        }
        this.prezzo = prezzo;
    }

    public String getVenditore() {
        return venditore;
    }

    public void setVenditore(String venditore) {
        if (venditore == null || venditore.trim().isEmpty() || venditore.trim().length() >= 100) {
            throw new IllegalArgumentException(
                    "Venditore non valido: non può essere nullo o vuoto o più lunga di 100 caratteri.");
        }
        this.venditore = venditore.trim();
    }
}