CREATE TABLE wohnungen (
    id              UUID            PRIMARY KEY,
    titel           VARCHAR(255)    NOT NULL,
    beschreibung    TEXT            NOT NULL,
    strasse         VARCHAR(255)    NOT NULL,
    hausnummer      VARCHAR(20)     NOT NULL,
    postleitzahl    VARCHAR(10)     NOT NULL,
    stadt           VARCHAR(100)    NOT NULL,
    quadratmeter    NUMERIC(6, 2)   NOT NULL,
    zimmeranzahl    NUMERIC(4, 1)   NOT NULL,
    mietpreis_cent  INTEGER         NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    erstellt_am     TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Index for the list view: we always filter by status and sort by creation date
CREATE INDEX idx_wohnungen_status_erstellt ON wohnungen (status, erstellt_am DESC);
