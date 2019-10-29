CREATE TABLE VARSEL(
  ID                        NUMBER(19, 0)               NOT NULL ,
  BEHANDLING_ID             NUMBER(19, 0)               NOT NULL ,
  AKTIV                     VARCHAR2(1)                 NOT NULL ,
  VARSEL_FRITEKST           VARCHAR2(3000 CHAR)         NOT NULL,
  VARSEL_BELOEP             NUMBER(12, 0),
  OPPRETTET_AV  VARCHAR2(200 CHAR) DEFAULT 'VL'         NOT NULL ,
  OPPRETTET_TID TIMESTAMP(3) DEFAULT systimestamp       NOT NULL ,
  ENDRET_AV     VARCHAR2(20 CHAR),
  ENDRET_TID    TIMESTAMP(3)
);

CREATE SEQUENCE SEQ_VARSEL MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE  VARSEL IS 'Tabell for å lagre varsel info';
COMMENT ON COLUMN VARSEL.ID IS 'Primary Key';
COMMENT ON COLUMN VARSEL.BEHANDLING_ID IS 'FK: BEHANDLING fremmednøkkel for tilknyttet behandling';
COMMENT ON COLUMN VARSEL.AKTIV IS 'Angir status av varsel';
COMMENT ON COLUMN VARSEL.VARSEL_FRITEKST IS 'fritekst som brukes i varselbrev';
COMMENT ON COLUMN VARSEL.VARSEL_BELOEP IS 'beløp som brukes i varselbrev';

--------------------------------------------------------
--  DDL for Index og Primary Key
--------------------------------------------------------

ALTER TABLE VARSEL ADD CONSTRAINT PK_VARSEL PRIMARY KEY ("ID") ENABLE;

CREATE INDEX IDX_VARSEL_1 ON VARSEL ("BEHANDLING_ID");

--------------------------------------------------------
--  Ref Constraints for Tabeller
--------------------------------------------------------

ALTER TABLE VARSEL ADD CONSTRAINT FK_VARSEL_1 FOREIGN KEY ("BEHANDLING_ID")
REFERENCES BEHANDLING("ID") ENABLE;

