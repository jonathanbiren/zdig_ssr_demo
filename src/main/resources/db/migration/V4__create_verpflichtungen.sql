CREATE TABLE verpflichtungen
(
    id           UUID         PRIMARY KEY,
    kunde_id     UUID         NOT NULL REFERENCES kunden(id) ON DELETE CASCADE,
    bezeichnung  VARCHAR(255) NOT NULL,
    betrag_cent  BIGINT       NOT NULL,
    erstellt_am  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_verpflichtungen_kunde_id ON verpflichtungen (kunde_id);
