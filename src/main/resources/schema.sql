CREATE TABLE IF NOT EXISTS articolo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(200) NOT NULL,
    descrizione VARCHAR(1000),
    immagine VARCHAR(150),
    prezzo INTEGER NOT NULL CHECK (prezzo > 0 AND prezzo < 500000),
    venditore VARCHAR(100) NOT NULL,
    FOREIGN KEY (venditore) REFERENCES utente(username),
    CHECK(length(nome) >= 3)
);

CREATE TABLE IF NOT EXISTS asta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(200) NOT NULL,
    descrizione VARCHAR(1000),
    immagine VARCHAR(150),
    scadenza VARCHAR(50) NOT NULL,
    rialzo_minimo INTEGER NOT NULL CHECK (rialzo_minimo > 0 AND rialzo_minimo < 100000),
    venditore VARCHAR(100) NOT NULL,
    aggiudicatario VARCHAR(100),
    FOREIGN KEY (venditore) REFERENCES utente(username),
    FOREIGN KEY (aggiudicatario) REFERENCES utente(username) ON DELETE SET NULL,
    CHECK(length(nome) >= 4),
    CHECK(length(scadenza) >= 4)
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
    username VARCHAR(100) NOT NULL,
    prezzo_offerto INTEGER NOT NULL CHECK (prezzo_offerto > 0 AND prezzo_offerto < 1000000),
    data_offerta VARCHAR(50) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_asta) REFERENCES asta(id),
    FOREIGN KEY (username) REFERENCES utente(username),
    CHECK(length(data_offerta) >= 4)
);

CREATE TABLE IF NOT EXISTS utente (
    username VARCHAR(100) PRIMARY KEY,
    pass_hash VARCHAR(250) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    indirizzo VARCHAR(400) NOT NULL,
    CHECK(length(username) >= 1),
    CHECK(length(pass_hash) >= 10),
    CHECK(length(nome) >= 2),
    CHECK(length(cognome) >= 2),
    CHECK(length(indirizzo) >= 6)
);