CREATE TABLE kunden
(
    id                      UUID PRIMARY KEY,
    kurzname                VARCHAR(30)   NOT NULL,
    name                    VARCHAR(255)  NOT NULL,
    zsNummer                BIGINT        NOT NULL,
    adresse                 VARCHAR(250)  NOT NULL,
    status                  VARCHAR(50)   NOT NULL,
    besondersSchuetzenswert BOOLEAN       NOT NULL DEFAULT false,
    erstellt_am             TIMESTAMPTZ   NOT NULL DEFAULT now()
);

