CREATE TABLE kunden
(
    id                      UUID PRIMARY KEY,
    kurzname                VARCHAR(30)   NOT NULL,
    name                    VARCHAR(255)  NOT NULL,
    zs_nummer               BIGINT        NOT NULL,
    adresse                 VARCHAR(250)  NOT NULL,
    status                  VARCHAR(50)   NOT NULL,
    besonders_schuetzenswert BOOLEAN       NOT NULL DEFAULT false,
    erstellt_am             TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE dienstgrade
(
    id                      INT           PRIMARY KEY,
    name                    VARCHAR(30)   NOT NULL,
    sortierung              INT           NOT NULL
);

CREATE TABLE personen
(
    id                      UUID PRIMARY KEY,
    dienstgrad_id           INT REFERENCES dienstgrade(id),
    vorname                 VARCHAR(255)  NOT NULL,
    nachname                VARCHAR(255)  NOT NULL,
    titel                   VARCHAR(255),
    persnr                  VARCHAR(8),
    pkbw                    VARCHAR(12),
    bmwkid                  VARCHAR(8),
    sue                     VARCHAR(30),
    datum_sue               DATE,
    datum_sue3_einleitung   DATE,
    ermaechtigungsgrad      VARCHAR(30),
    datum_ermaechtigung     DATE,
    datum_freigabe_bis      DATE,
    datum_sondergenehmigung_ab DATE,
    email                   VARCHAR(255),
    gruppen_mailbox         VARCHAR(255),
    hinweis                 VARCHAR(255),
    tel_ziv                 VARCHAR(255),
    tel_mil                 VARCHAR(255),
    erstellt_am             TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE verpflichtungen
(
    id           UUID         PRIMARY KEY,
    kunde_id     UUID         NOT NULL REFERENCES kunden(id),
    person_id    UUID         NOT NULL REFERENCES personen(id),
    bezeichnung  VARCHAR(255) NOT NULL,
    betrag_cent  BIGINT       NOT NULL,
    erstellt_am  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_verpflichtungen_kunde_id ON verpflichtungen (kunde_id);
CREATE INDEX idx_verpflichtungen_person_id ON verpflichtungen (person_id);
