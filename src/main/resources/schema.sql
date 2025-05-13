CREATE TABLE IF NOT EXISTS articolo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descrizione TEXT,
    immagine TEXT,
    prezzo REAL NOT NULL,
    venditore TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS asta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descrizione TEXT,
    immagine TEXT,
    scadenza TEXT NOT NULL,
    venditore TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS asta_articoli (
    id_asta INTEGER NOT NULL,
    id_articolo INTEGER NOT NULL,
    PRIMARY KEY (id_asta, id_articolo),
    FOREIGN KEY (id_asta) REFERENCES asta(id),
    FOREIGN KEY (id_articolo) REFERENCES articolo(id)
);

CREATE TABLE offerta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_asta INTEGER NOT NULL,
    username TEXT NOT NULL,
    prezzo_offerto REAL NOT NULL,
    data_offerta TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_asta) REFERENCES asta(id),
    FOREIGN KEY (username) REFERENCES utente(username)
);
