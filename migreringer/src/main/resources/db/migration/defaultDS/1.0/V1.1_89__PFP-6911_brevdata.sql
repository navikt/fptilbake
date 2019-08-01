create sequence SEQ_VARSELBREV_DATA minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;

CREATE TABLE VARSELBREV_DATA
(
  ID             NUMBER(19, 0) NOT NULL,
  BEHANDLING_ID  NUMBER(19, 0) NOT NULL,
  JOURNALPOST_ID VARCHAR2(20 CHAR),
  DOKUMENT_ID    VARCHAR2(20 CHAR),
  OPPRETTET_AV   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  OPPRETTET_TID  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  ENDRET_AV      VARCHAR2(20 CHAR),
  ENDRET_TID     TIMESTAMP(3),
  CONSTRAINT PK_VARSELBREV_DATA PRIMARY KEY (ID),
  CONSTRAINT FK_VARSELBREV_DATA FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING (ID) ENABLE
);

CREATE INDEX IDX_VARSELBREVDATA ON VARSELBREV_DATA (BEHANDLING_ID);

COMMENT ON COLUMN VARSELBREV_DATA.ID IS 'Primary Key';
COMMENT ON COLUMN VARSELBREV_DATA.BEHANDLING_ID IS 'FK: BEHANDLING Fremmednøkkel for kobling til behandling i fptilbake';
COMMENT ON COLUMN VARSELBREV_DATA.JOURNALPOST_ID IS 'Journalpostid i Doksys';
COMMENT ON COLUMN VARSELBREV_DATA.DOKUMENT_ID IS 'Dokumentid i Doksys';
COMMENT ON TABLE VARSELBREV_DATA  IS 'Varselbrevdata innholder informasjon om varselbrev som er bestilt.';

