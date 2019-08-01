--------------------------------------------------------
--  DDL for PROSESS_TASK_FEILHAND
--------------------------------------------------------

  CREATE TABLE PROSESS_TASK_FEILHAND (
    KODE            VARCHAR2(20 CHAR)                     NOT NULL,
	  NAVN            VARCHAR2(50 CHAR)                     NOT NULL,
	  BESKRIVELSE     VARCHAR2(2000 CHAR),
	  INPUT_VARIABEL1 NUMBER,
	  INPUT_VARIABEL2 NUMBER,
	  OPPRETTET_AV    VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
	  OPPRETTET_TID   TIMESTAMP (3)     DEFAULT systimestamp NOT NULL,
	  ENDRET_AV       VARCHAR2(20 CHAR),
	  ENDRET_TID      TIMESTAMP (3),
	  CONSTRAINT PK_PROSESS_TASK_FEILHAND PRIMARY KEY (KODE)
   );

   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.KODE IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.NAVN IS 'Lesbart navn på type feilhåndtering brukt i prosesstask';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.BESKRIVELSE IS 'Utdypende beskrivelse av koden';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.INPUT_VARIABEL1 IS 'Variabel 1: Dynamisk konfigurasjon for en feilhåndteringsstrategi.  Verdi og betydning er bestemt av feilhåndteringsalgoritmen';
   COMMENT ON COLUMN PROSESS_TASK_FEILHAND.INPUT_VARIABEL2 IS 'Variabel 2: Dynamisk konfigurasjon for en feilhåndteringsstrategi.  Verdi og betydning er bestemt av feilhåndteringsalgoritmen';
   COMMENT ON TABLE PROSESS_TASK_FEILHAND  IS 'Kodetabell for feilhåndterings-metoder. For eksempel antall ganger å prøve på nytt og til hvilke tidspunkt';

--------------------------------------------------------
--  DDL for PROSESS_TASK_TYPE
--------------------------------------------------------

  CREATE TABLE PROSESS_TASK_TYPE (
    KODE                     VARCHAR2(50 CHAR)                        NOT NULL,
	  NAVN                     VARCHAR2(50 CHAR),
	  FEIL_MAKS_FORSOEK        NUMBER(10,0)        DEFAULT 1            NOT NULL,
	  FEIL_SEK_MELLOM_FORSOEK  NUMBER(10,0)        DEFAULT 30           NOT NULL,
	  FEILHANDTERING_ALGORITME VARCHAR2(20 CHAR)   DEFAULT 'DEFAULT',
	  BESKRIVELSE              VARCHAR2(2000 CHAR),
	  OPPRETTET_AV             VARCHAR2(20 CHAR)   DEFAULT 'VL'         NOT NULL,
	  OPPRETTET_TID            TIMESTAMP (3)       DEFAULT systimestamp NOT NULL,
	  ENDRET_AV                VARCHAR2(20 CHAR),
	  ENDRET_TID               TIMESTAMP (3),
	  CONSTRAINT PK_PROSESS_TASK_TYPE   PRIMARY KEY (KODE),
	  CONSTRAINT FK_PROSESS_TASK_TYPE_1 FOREIGN KEY (FEILHANDTERING_ALGORITME) REFERENCES PROSESS_TASK_FEILHAND (KODE)
   );

   COMMENT ON COLUMN PROSESS_TASK_TYPE.KODE IS 'Kodeverk Primary Key';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.NAVN IS 'Lesbart navn på prosesstasktype';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEIL_MAKS_FORSOEK IS 'Maksimalt anntall forsøk på rekjøring om noe går galt';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEIL_SEK_MELLOM_FORSOEK IS 'Ventetid i sekunder mellom hvert forsøk på rekjøring om noe har gått galt';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.FEILHANDTERING_ALGORITME IS 'FK:PROSESS_TASK_FEILHAND Fremmednøkkel til tabell som viser detaljer om hvordan en feilsituasjon skal håndteres';
   COMMENT ON COLUMN PROSESS_TASK_TYPE.BESKRIVELSE IS 'Utdypende beskrivelse av koden';
   COMMENT ON TABLE PROSESS_TASK_TYPE  IS 'Kodetabell for typer prosesser med beskrivelse og informasjon om hvilken feilhåndteringen som skal benyttes';

  CREATE INDEX IDX_PROSESS_TASK_TYPE_1 ON PROSESS_TASK_TYPE (FEILHANDTERING_ALGORITME);

--------------------------------------------------------
--  DDL for PROSESS_TASK
--------------------------------------------------------

  CREATE TABLE PROSESS_TASK 
   (	ID                        NUMBER(19,0)                                  NOT NULL,
	    TASK_TYPE                 VARCHAR2(50 CHAR)                             NOT NULL,
	    PRIORITET                 NUMBER(3,0)        DEFAULT 0                  NOT NULL,
	    STATUS                    VARCHAR2(20 CHAR)  DEFAULT 'KLAR'             NOT NULL,
	    TASK_PARAMETERE           VARCHAR2(4000 CHAR),
	    TASK_PAYLOAD              CLOB,
	    TASK_GRUPPE               VARCHAR2(250 CHAR),
	    TASK_SEKVENS              VARCHAR2(100 CHAR) DEFAULT '1'                NOT NULL,
	    NESTE_KJOERING_ETTER      TIMESTAMP (0)      DEFAULT current_timestamp,
	    FEILEDE_FORSOEK           NUMBER(5,0)        DEFAULT 0,
	    SISTE_KJOERING_TS         TIMESTAMP (6),
	    SISTE_KJOERING_FEIL_KODE  VARCHAR2(50 CHAR),
	    SISTE_KJOERING_FEIL_TEKST CLOB,
	    SISTE_KJOERING_SERVER     VARCHAR2(50 CHAR),
	    VERSJON                   NUMBER(19,0)       DEFAULT 0                  NOT NULL,
	    CONSTRAINT PK_PROSESS_TASK         PRIMARY KEY (ID),
	    CONSTRAINT FK_PROSESS_TASK_1       FOREIGN KEY (TASK_TYPE) REFERENCES PROSESS_TASK_TYPE (KODE),
	    CONSTRAINT CHK_PROSESS_TASK_STATUS CHECK (status IN ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'FERDIG'))
   );

   CREATE INDEX IDX_PROSESS_TASK_1 ON PROSESS_TASK (STATUS);
   CREATE INDEX IDX_PROSESS_TASK_2 ON PROSESS_TASK (TASK_TYPE);
   CREATE INDEX IDX_PROSESS_TASK_3 ON PROSESS_TASK (NESTE_KJOERING_ETTER);
   CREATE INDEX IDX_PROSESS_TASK_4 ON PROSESS_TASK (TASK_GRUPPE);

   COMMENT ON COLUMN PROSESS_TASK.ID IS 'Primary Key';
   COMMENT ON COLUMN PROSESS_TASK.TASK_TYPE IS 'navn på task. Brukes til å matche riktig implementasjon';
   COMMENT ON COLUMN PROSESS_TASK.PRIORITET IS 'prioritet på task.  Høyere tall har høyere prioritet';
   COMMENT ON COLUMN PROSESS_TASK.STATUS IS 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';
   COMMENT ON COLUMN PROSESS_TASK.TASK_PARAMETERE IS 'parametere angitt for en task';
   COMMENT ON COLUMN PROSESS_TASK.TASK_PAYLOAD IS 'inputdata for en task';
   COMMENT ON COLUMN PROSESS_TASK.TASK_GRUPPE IS 'angir en unik id som grupperer flere ';
   COMMENT ON COLUMN PROSESS_TASK.TASK_SEKVENS IS 'angir rekkefølge på task innenfor en gruppe ';
   COMMENT ON COLUMN PROSESS_TASK.NESTE_KJOERING_ETTER IS 'tasken skal ikke kjøeres før tidspunkt er passert';
   COMMENT ON COLUMN PROSESS_TASK.FEILEDE_FORSOEK IS 'antall feilede forsøk';
   COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_TS IS 'siste gang tasken ble forsøkt kjørt';
   COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_FEIL_KODE IS 'siste feilkode tasken fikk';
   COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_FEIL_TEKST IS 'siste feil tasken fikk';
   COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_SERVER IS 'navn på node som sist kjørte en task (server@pid)';
   COMMENT ON COLUMN PROSESS_TASK.VERSJON IS 'angir versjon for optimistisk låsing';
   COMMENT ON TABLE PROSESS_TASK  IS 'Inneholder tasks som skal kjøres i bakgrunnen';


--------------------------------------------------------
--  DDL for FAGSAK_PROSESS_TASK
--------------------------------------------------------

  CREATE TABLE FAGSAK_PROSESS_TASK (
    ID               NUMBER(19,0)                           NOT NULL,
	  FAGSAK_ID        NUMBER(19,0)                           NOT NULL,
	  PROSESS_TASK_ID  NUMBER(19,0)                           NOT NULL,
	  BEHANDLING_ID    NUMBER(19,0),
	  VERSJON          NUMBER(19,0)      DEFAULT 0            NOT NULL,
	  OPPRETTET_AV     VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
	  OPPRETTET_TID    TIMESTAMP (3)     DEFAULT systimestamp NOT NULL,
	  ENDRET_AV        VARCHAR2(20 CHAR),
	  ENDRET_TID       TIMESTAMP (3),
	  GRUPPE_SEKVENSNR NUMBER(19,0),
	  CONSTRAINT PK_FAGSAK_PROSESS_TASK PRIMARY KEY (ID),
	  CONSTRAINT FK_FAGSAK_PROSESS_TASK_1 FOREIGN KEY (FAGSAK_ID) REFERENCES FAGSAK (ID),
	  CONSTRAINT FK_FAGSAK_PROSESS_TASK_2 FOREIGN KEY (PROSESS_TASK_ID) REFERENCES PROSESS_TASK (ID),
	  CONSTRAINT FK_FAGSAK_PROSESS_TASK_3 FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING (ID)
   );

   CREATE SEQUENCE SEQ_FAGSAK_PROSESS_TASK MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

   CREATE UNIQUE INDEX UIDX_FAGSAK_PROSESS_TASK_1 ON FAGSAK_PROSESS_TASK (FAGSAK_ID, PROSESS_TASK_ID);
   CREATE INDEX IDX_FAGSAK_PROSESS_TASK_1 ON FAGSAK_PROSESS_TASK (FAGSAK_ID);
   CREATE INDEX IDX_FAGSAK_PROSESS_TASK_2 ON FAGSAK_PROSESS_TASK (PROSESS_TASK_ID);
   CREATE INDEX IDX_FAGSAK_PROSESS_TASK_3 ON FAGSAK_PROSESS_TASK (BEHANDLING_ID);
   CREATE INDEX IDX_FAGSAK_PROSESS_TASK_4 ON FAGSAK_PROSESS_TASK (GRUPPE_SEKVENSNR);

   COMMENT ON COLUMN FAGSAK_PROSESS_TASK.ID IS 'Primærnøkkel';
   COMMENT ON COLUMN FAGSAK_PROSESS_TASK.FAGSAK_ID IS 'FK: Fremmednøkkel for kobling til fagsak';
   COMMENT ON COLUMN FAGSAK_PROSESS_TASK.PROSESS_TASK_ID IS 'FK: Fremmednøkkel for knyttning til logging av prosesstask som ???';
   COMMENT ON COLUMN FAGSAK_PROSESS_TASK.BEHANDLING_ID IS 'FK: Fremmednøkkel for kobling til behandling';
   COMMENT ON COLUMN FAGSAK_PROSESS_TASK.GRUPPE_SEKVENSNR IS 'For en gitt fagsak angir hvilken rekkefølge task skal kjøres.  Kun tasks med laveste gruppe_sekvensnr vil kjøres. Når disse er FERDIG vil de ryddes bort og neste med lavest sekvensnr kan kjøres (gitt at den er KLAR)';
   COMMENT ON TABLE FAGSAK_PROSESS_TASK  IS '1-M relasjonstabell for ? mappe fagsak til prosess_tasks (som ikke er FERDIG)';

