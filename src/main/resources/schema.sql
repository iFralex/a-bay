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
    rialzo_minimo INTEGER NOT NULL,
    venditore TEXT NOT NULL,
    aggiudicatario VARCHAR(255),
    FOREIGN KEY (venditore) REFERENCES utente(username),
    FOREIGN KEY (aggiudicatario) REFERENCES utente(username) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS asta_articoli (
    id_asta INTEGER NOT NULL,
    id_articolo INTEGER NOT NULL,
    PRIMARY KEY (id_asta, id_articolo),
    FOREIGN KEY (id_asta) REFERENCES asta(id),
    FOREIGN KEY (id_articolo) REFERENCES articolo(id)
);

CREATE TABLE IF NOT EXISTS offerta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_asta INTEGER NOT NULL,
    username TEXT NOT NULL,
    prezzo_offerto REAL NOT NULL,
    data_offerta TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_asta) REFERENCES asta(id),
    FOREIGN KEY (username) REFERENCES utente(username)
);

CREATE TABLE IF NOT EXISTS utente (
    username TEXT PRIMARY KEY,
    pass_hash TEXT NOT NULL,
    nome TEXT NOT NULL,
    cognome TEXT NOT NULL,
    indirizzo TEXT NOT NULL
);
