package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import utils.TimeUtils;

public class Asta {
    public static class Offerta {
        private String username;
        private double prezzo;
        private LocalDateTime date;

        public Offerta(String username, double prezzo, LocalDateTime date) {
            setUsername(username);
            setPrezzo(prezzo);
            setDate(date);
        }

        public String getUsername() {
            return username;
        }

        public double getPrezzo() {
            return prezzo;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPrezzo(double prezzo) {
            if (prezzo < 0)
                throw new IllegalArgumentException("Prezzo negativo");
            this.prezzo = prezzo;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }
    }

    private int id;
    private String usernameVenditore;
    private List<Articolo> articoli;
    private List<Integer> idArticoli; // usato in input
    private int rialzoMinimo;
    private LocalDateTime scadenza;
    private String tempoRimasto;
    private boolean chiusa;
    private Offerta offertaMassima;
    private String nome;
    private String descrizione;
    private String imageUrl;
    private List<Offerta> offerte = new ArrayList<>();

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVenditore() {return this.usernameVenditore;}

    public void setVenditore(String usernameVenditore) {this.usernameVenditore = usernameVenditore;}

    public List<Articolo> getArticoli() {
        return articoli;
    }

    public void setArticoli(List<Articolo> articoli) {
        this.articoli = articoli;
    }

    public List<Integer> getIdArticoli() {
        return idArticoli;
    }

    public void setIdArticoli(List<Integer> idArticoli) {
        this.idArticoli = idArticoli;
    }

    public int getRialzoMinimo() {
        return rialzoMinimo;
    }

    public void setRialzoMinimo(int rialzoMinimo) {
        this.rialzoMinimo = rialzoMinimo;
    }

    public LocalDateTime getScadenza() {
        return scadenza;
    }

    public String getTempoRimasto() {
        return tempoRimasto;
    }

    public void setScadenza(LocalDateTime scadenza) {
        this.scadenza = scadenza;
        tempoRimasto = TimeUtils.getTempoMancante(scadenza);
    }

    public boolean isChiusa() {
        return chiusa;
    }

    public void setChiusa(boolean chiusa) {
        this.chiusa = chiusa;
    }

    public Offerta getOffertaMassima() {
        if (offertaMassima == null)
            offertaMassima = offerte.stream()
                    .max(Comparator.comparingDouble(o -> o.prezzo))
                    .orElse(null);
        return offertaMassima;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setImmagine(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNome() {
        return this.nome;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public String getImmagine() {
        return this.imageUrl;
    }

    public void newOfferta(String username, double prezzo) {
        Offerta offerta = new Offerta(username, prezzo, LocalDateTime.now());
        this.offerte.add(offerta);
        if (offertaMassima == null || prezzo > offertaMassima.prezzo)
            this.offertaMassima = offerta;
    }

    public void setOfferte(List<Offerta> offerte) {
        this.offerte = offerte;
    }

    public List<Offerta> getOfferte() {
        return this.offerte;
    }

    public double getPrezzoIniziale() {
        return offerte.get(0).prezzo;
    }

    public Offerta getOffertaVincitrice() {
        return isChiusa() ? getOffertaMassima() : null;
    }
}