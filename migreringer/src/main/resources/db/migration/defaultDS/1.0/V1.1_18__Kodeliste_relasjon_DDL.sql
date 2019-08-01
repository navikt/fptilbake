--------------------------------------------------------
--  DDL for Table KODELISTE_RELASJON
--------------------------------------------------------

CREATE TABLE KODELISTE_RELASJON (
  ID            NUMBER(19, 0)                                     NOT NULL,
  KODEVERK1     VARCHAR2(100 CHAR)                                NOT NULL,
  KODE1         VARCHAR2(100 CHAR)                                NOT NULL,
  KODEVERK2     VARCHAR2(100 CHAR)                                NOT NULL,
  KODE2         VARCHAR2(100 CHAR)                                NOT NULL,
  GYLDIG_FOM    DATE DEFAULT SYSDATE                              NOT NULL,
  GYLDIG_TOM    DATE DEFAULT TO_DATE('31.12.9999', 'DD.MM.YYYY')  NOT NULL,
  OPPRETTET_AV  VARCHAR2(20 CHAR) DEFAULT 'VL'                    NOT NULL,
  OPPRETTET_TID TIMESTAMP(3) DEFAULT systimestamp                 NOT NULL,
  ENDRET_AV     VARCHAR2(20 CHAR),
  ENDRET_TID    TIMESTAMP(3),
  CONSTRAINT PK_KODELISTE_RELASJON PRIMARY KEY (ID)
);

CREATE SEQUENCE seq_kodeliste_relasjon MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_KODELISTE_RELASJON_6 ON KODELISTE_RELASJON (KODE1, KODEVERK1);
CREATE INDEX IDX_KODELISTE_RELASJON_7 ON KODELISTE_RELASJON (KODE2, KODEVERK2);
CREATE INDEX IDX_KODELISTE_RELASJON_1 ON KODELISTE_RELASJON (KODEVERK1, KODE1);
CREATE INDEX IDX_KODELISTE_RELASJON_2 ON KODELISTE_RELASJON (KODEVERK2, KODE2);
CREATE UNIQUE INDEX UIDX_KODELISTE_RELASJON_1 ON KODELISTE_RELASJON (KODEVERK1, KODE1, KODEVERK2, KODE2);

ALTER TABLE KODELISTE_RELASJON ADD CONSTRAINT UIDX_KODELISTE_RELASJON_1 UNIQUE (KODEVERK1, KODE1, KODEVERK2, KODE2);
ALTER TABLE KODELISTE_RELASJON ADD CONSTRAINT FK_KODELISTE_RELASJON_1 FOREIGN KEY (KODE1, KODEVERK1) REFERENCES KODELISTE (KODE, KODEVERK) ENABLE;
ALTER TABLE KODELISTE_RELASJON ADD CONSTRAINT FK_KODELISTE_RELASJON_2 FOREIGN KEY (KODE2, KODEVERK2) REFERENCES KODELISTE (KODE, KODEVERK) ENABLE;

COMMENT ON COLUMN KODELISTE_RELASJON.ID IS 'Primary Key';
COMMENT ON COLUMN KODELISTE_RELASJON.KODEVERK1 IS 'Kodeverk for kode 1';
COMMENT ON COLUMN KODELISTE_RELASJON.KODE1 IS 'Kode 1';
COMMENT ON COLUMN KODELISTE_RELASJON.KODEVERK2 IS 'Kodeverk for kode 2';
COMMENT ON COLUMN KODELISTE_RELASJON.KODE2 IS 'Kode 2';
COMMENT ON COLUMN KODELISTE_RELASJON.GYLDIG_FOM IS 'Gyldig fra og med dato';
COMMENT ON COLUMN KODELISTE_RELASJON.GYLDIG_TOM IS 'Gyldig til og med dato';
COMMENT ON TABLE KODELISTE_RELASJON  IS 'Relasjon mellom kodeliste elementer: kode1 og kode2';