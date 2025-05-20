package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import utils.TimeUtils;

public class Asta {
    public static class Offerta {
        private String username;
        private int prezzo;
        private LocalDateTime date;
        private String formattedDate;

        public Offerta(String username, int prezzo) {
            this(username, prezzo, LocalDateTime.now());
        }

        public Offerta(String username, int prezzo, LocalDateTime date) {
            setUsername(username);
            setPrezzo(prezzo);
            setDate(date);
        }

        public String getUsername() {
            return username;
        }

        public int getPrezzo() {
            return prezzo;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getFormattedDate() {
            return formattedDate;
        }

        public void setUsername(String username) {
            if (username == null || username.trim().isEmpty() && username.trim().length() >= 100) {
                throw new IllegalArgumentException(
                        "Username non valido: non può essere nullo o vuoto o maggiore di 100 caratteri.");
            }
            this.username = username.trim();
        }

        public void setPrezzo(int prezzo) {
            if (prezzo <= 0 && prezzo >= 1000000) {
                throw new IllegalArgumentException(
                        "Prezzo non valido: deve essere maggiore di zero e minore di 1000000.");
            }
            this.prezzo = prezzo;
        }

        public void setDate(LocalDateTime date) {
            if (date == null) {
                throw new IllegalArgumentException("Data offerta non valida: non può essere nulla.");
            }
            this.date = date;
            this.formattedDate = TimeUtils.formattaDataOra(date);
        }
    }

    private int id;
    private List<Articolo> articoli;
    private List<Integer> idArticoli; // usato in input
    private int rialzoMinimo;
    private LocalDateTime scadenza;
    private String tempoRimasto;
    private String formattedScadenza;
    private boolean chiusa;
    private Offerta offertaMassima;
    private String nome;
    private String descrizione;
    private String encodedImage; // base64 for display
    private List<Offerta> offerte = new ArrayList<>();

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVenditore() {
        return getOfferte().size() > 0 ? getOfferte().get(0).getUsername() : "";
    }

    public List<Articolo> getArticoli() {
        return articoli;
    }

    public void setArticoli(List<Articolo> articoli) {
        if (articoli == null || articoli.size() == 0) {
            throw new IllegalArgumentException("articoli non valido: non può essere nullo o vuoto.");
        }
        this.articoli = articoli;
    }

    public List<Integer> getIdArticoli() {
        return idArticoli;
    }

    public void setIdArticoli(List<Integer> idArticoli) {
        if (idArticoli == null || idArticoli.size() == 0) {
            throw new IllegalArgumentException("articoli non valido: non può essere nullo o vuoto.");
        }
        this.idArticoli = idArticoli;
    }

    public int getRialzoMinimo() {
        return rialzoMinimo;
    }

    public void setRialzoMinimo(int rialzoMinimo) {
        if (rialzoMinimo <= 0 || rialzoMinimo > 100000) {
            throw new IllegalArgumentException(
                    "rialzoMinimo non valido: non può essere nullo o minore di 0 o maggiore di 100000.");
        }
        this.rialzoMinimo = rialzoMinimo;
    }

    public LocalDateTime getScadenza() {
        return scadenza;
    }

    public String getTempoRimasto() {
        return tempoRimasto;
    }

    public String getFormattedScadenza() {
        return formattedScadenza;
    }

    public void setScadenza(LocalDateTime scadenza) {
        if (scadenza == null) {
            throw new IllegalArgumentException("scadenza non valida: non può essere nulla.");
        }
        this.scadenza = scadenza;
        tempoRimasto = TimeUtils.getTempoMancante(scadenza);
        formattedScadenza = TimeUtils.formattaDataOra(scadenza);
    }

    public boolean isChiusa() {
        return chiusa;
    }

    public void setChiusa(boolean chiusa) {
        this.chiusa = chiusa;
    }

    public Offerta getOffertaMassima() {
        if (offertaMassima == null && getOfferteSenzaVenditore().size() > 0)
            offertaMassima = getOfferteSenzaVenditore().get(getOfferteSenzaVenditore().size() - 1);
        return offertaMassima;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().length() < 4 || nome.trim().length() >= 200) {
            throw new IllegalArgumentException(
                    "nome non valido: non può essere nullo o più breve di 4 caratteri o più lungo di 200.");
        }
        this.nome = nome.trim();
    }

    public void setDescrizione(String descrizione) {
        if (descrizione != null && descrizione.trim().length() >= 1000) {
            throw new IllegalArgumentException("descrizione non valido: non può essere più lungo di 1000 caratteri.");
        }
        this.descrizione = descrizione.trim();
    }

    public void setImmagine(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public String getImmagine() {
        return this.encodedImage;
    }

    public String getNome() {
        return this.nome;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public void newOfferta(Offerta offerta) {
        newOfferta(offerta.getUsername(), offerta.getPrezzo(), offerta.getDate());
    }

    public void newOfferta(String username, int prezzo) {
        newOfferta(username, prezzo, LocalDateTime.now());
    }

    public void newOfferta(String username, int prezzo, LocalDateTime date) {
        // Se l'asta è chiusa, non si può fare offerta
        if (isChiusa())
            return;

        // Il venditore non può fare offerte sulla propria asta
        if (getOfferte().size() > 0 && username.equals(getVenditore()))
            return;

        // Se esiste già un'offerta massima, il prezzo deve superarla di almeno il
        // rialzo minimo
        Offerta offertaMassimaCorrente = getOffertaMassima();
        if (offertaMassimaCorrente != null && prezzo <= offertaMassimaCorrente.getPrezzo() + getRialzoMinimo())
            return;

        // Crea e aggiungi la nuova offerta
        Offerta nuovaOfferta = new Offerta(username, prezzo, date);
        this.offerte.add(nuovaOfferta);

        // Aggiorna l'offerta massima se necessario
        if (offertaMassimaCorrente == null || prezzo > offertaMassimaCorrente.getPrezzo()) {
            this.offertaMassima = nuovaOfferta;
        }
    }

    public void setOfferte(List<Offerta> offerte) {
        for (int i = 1; i < offerte.size(); i++) {
            if (offerte.get(i).getPrezzo() < offerte.get(i - 1).getPrezzo()) {
                throw new IllegalArgumentException("Lista non ordinata.");
            }
        }
        this.offerte = offerte;
    }

    public List<Offerta> getOfferte() {
        return this.offerte;
    }

    public List<Offerta> getOfferteSenzaVenditore() {
        return getOfferteSenzaVenditore(false);
    }

    public List<Offerta> getOfferteSenzaVenditore(Boolean reversed) {
        List<Offerta> offerte = this.getOfferte();
        if (offerte.size() <= 1) {
            return new ArrayList<>();
        }

        List<Offerta> result = new ArrayList<>(offerte.subList(1, offerte.size()));

        if (reversed) {
            Collections.reverse(result);
        }

        return result;
    }
    public int getPrezzoIniziale() {
        return getOfferte().size() > 0 ? getOfferte().get(0).getPrezzo() : 0;
    }

    public Offerta getOffertaVincitrice() {
        return isChiusa() ? getOffertaMassima() : null;
    }
}