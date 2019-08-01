CREATE TABLE KRAVGRUNNLAG_XML(
  ID                      NUMBER(19, 0)      NOT NULL,
  MELDING                 clob not null,
  EKSTERN_BEHANDLING_ID   varchar2(100 char),
  SEKVENS                 number(19, 0),
  OPPRETTET_AV            VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
  OPPRETTET_TID           TIMESTAMP(3) DEFAULT systimestamp      NOT NULL,
  ENDRET_AV               VARCHAR2(20 CHAR),
  ENDRET_TID              TIMESTAMP(3),
  CONSTRAINT PK_KRAVGRUNNLAG PRIMARY KEY (ID)
);

CREATE SEQUENCE SEQ_KRAVGRUNNLAG_XML MINVALUE 1 START WITH 1000000 INCREMENT BY 50 NOCACHE NOCYCLE;

comment on table KRAVGRUNNLAG_XML is 'Lagrer mottat kravgrunnlag-xml i påvente av at den skal prosesseres. Brukes for at mottak skal være mer robust';
comment on column KRAVGRUNNLAG_XML.ID is 'Primærnøkkel';
comment on column KRAVGRUNNLAG_XML.MELDING is 'Kravgrunnlag-xml';
comment on column KRAVGRUNNLAG_XML.EKSTERN_BEHANDLING_ID is 'Kobling til ekstern behandling';
comment on column KRAVGRUNNLAG_XML.SEKVENS is 'Teller innenfor en behandling';

create index idx_kravgrunnlag_xml_1 on KRAVGRUNNLAG_XML(EKSTERN_BEHANDLING_ID);
