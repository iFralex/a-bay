package model;

import utils.TimeUtils;

import java.time.LocalDateTime;
import java.io.Serializable;


public class Offerta implements Serializable{
    private String username;
    private int prezzo;
    private LocalDateTime date;
    private String formattedDate;

    public Offerta() {
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
