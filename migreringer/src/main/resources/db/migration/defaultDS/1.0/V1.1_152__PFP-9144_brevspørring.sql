INSERT INTO KODEVERK (KODE,KODEVERK_EIER,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID,SAMMENSATT)
VALUES ('BREV_TYPE','VL','N','N','Brev type','Kodeverk over forskjellige brev type som kan sendes for en behandling','VL',to_timestamp(sysdate,'DD.MM.RRRR'),null,null,'N');

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM) VALUES (seq_kodeliste.nextval,'BREV_TYPE','VARSEL',null,
'Varselbrev',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM) VALUES (seq_kodeliste.nextval,'BREV_TYPE','VEDTAK',null,
'VedtaksBrev',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM) VALUES (seq_kodeliste.nextval,'BREV_TYPE','HENLEGGELSE',null,
'Henleggelsesbrev',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM) VALUES (seq_kodeliste.nextval,'BREV_TYPE','-',null,
'Udefinert',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

CREATE SEQUENCE SEQ_BREV_SPORING MINVALUE 1000000 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE TABLE BREV_SPORING
(
  ID             NUMBER(19, 0) NOT NULL,
  BEHANDLING_ID  NUMBER(19, 0) NOT NULL,
  JOURNALPOST_ID VARCHAR2(20 CHAR) NOT NULL,
  DOKUMENT_ID    VARCHAR2(20 CHAR) NOT NULL,
  BREV_TYPE      VARCHAR2(100 CHAR) NOT NULL,
  KL_BREV_TYPE   VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('BREV_TYPE') VIRTUAL VISIBLE,
  OPPRETTET_AV   VARCHAR2(20 CHAR) DEFAULT 'VL' NOT NULL,
  OPPRETTET_TID  TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  ENDRET_AV      VARCHAR2(20 CHAR),
  ENDRET_TID     TIMESTAMP(3),
  CONSTRAINT PK_BREV_SPORING PRIMARY KEY (ID),
  CONSTRAINT FK_BREV_SPORING FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING (ID) ENABLE
);

CREATE INDEX IDX_BREV_SPORING ON BREV_SPORING (BEHANDLING_ID);
CREATE INDEX IDX_BREV_SPORING_1 ON BREV_SPORING (BREV_TYPE);

ALTER TABLE BREV_SPORING ADD CONSTRAINT FK_BREV_SPORING_1 FOREIGN KEY (BREV_TYPE, KL_BREV_TYPE) REFERENCES KODELISTE (KODE, KODEVERK) ENABLE;

COMMENT ON COLUMN BREV_SPORING.ID IS 'Primary Key';
COMMENT ON COLUMN BREV_SPORING.BEHANDLING_ID IS 'FK: BEHANDLING Fremmednøkkel for kobling til behandling i fptilbake';
COMMENT ON COLUMN BREV_SPORING.JOURNALPOST_ID IS 'Journalpostid i Doksys';
COMMENT ON COLUMN BREV_SPORING.DOKUMENT_ID IS 'Dokumentid i Doksys';
COMMENT ON COLUMN BREV_SPORING.BREV_TYPE IS 'Bestilt brev type';
COMMENT ON COLUMN BREV_SPORING.KL_BREV_TYPE IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
COMMENT ON TABLE BREV_SPORING  IS 'Brevsporing inneholder informasjon om forkjelige brev som er bestilt.';

