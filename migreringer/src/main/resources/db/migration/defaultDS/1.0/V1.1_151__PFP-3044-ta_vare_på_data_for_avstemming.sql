CREATE TABLE IVERKSATT_VEDTAK (
  ID                      NUMBER(19, 0)      NOT NULL,
  behandling_id           NUMBER(19, 0)      NOT NULL,
  iverksatt_dato          DATE               NOT NULL,
  oko_vedtak_id           VARCHAR2(255 CHAR) NOT NULL,
  brutto_tilbakekreves    NUMBER(19,2)       NOT NULL,
  netto_tilbakekreves     NUMBER(19,2)       NOT NULL,
  renter                  NUMBER(19,2)       NOT NULL,
  skatt                   NUMBER(19,2)       NOT NULL,
  OPPRETTET_AV            VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL ,
  OPPRETTET_TID           TIMESTAMP(3) DEFAULT systimestamp      NOT NULL ,
  ENDRET_AV               VARCHAR2(20 CHAR),
  ENDRET_TID              TIMESTAMP(3),
  VERSJON                 NUMBER(19,0)       DEFAULT 0                  NOT NULL,
  constraint PK_IVERKSATT_VEDTAK primary key (id),
  constraint FK_IVERKSATT_VEDTAK_1 foreign key (behandling_id) references BEHANDLING(ID)
);

CREATE SEQUENCE SEQ_IVERKSATT_VEDTAK MINVALUE 1 START WITH 1000050 INCREMENT BY 50 NOCACHE NOCYCLE;

create index IDX_IVERKSATT_VEDTAK_1 on IVERKSATT_VEDTAK(iverksatt_dato);
create index IDX_IVERKSATT_VEDTAK_2 on IVERKSATT_VEDTAK(behandling_id);

comment on table IVERKSATT_VEDTAK is 'Versjonering av vilkårsvurdering for tilbakekreving';
comment on column IVERKSATT_VEDTAK.behandling_id is 'Referanse til behandling';
comment on column IVERKSATT_VEDTAK.iverksatt_dato is 'Dato for når iverksettelse ble oversendt OK til økonomi';
comment on column IVERKSATT_VEDTAK.oko_vedtak_id is 'Referanse til vedtak_id i kravgrunnlaget';
comment on column IVERKSATT_VEDTAK.brutto_tilbakekreves is 'Totalt tilbakekrevesbeløp, brutto, uten renter';
comment on column IVERKSATT_VEDTAK.netto_tilbakekreves is 'Totalt tilbakekrevesbeløp, netto, uten renter';
comment on column IVERKSATT_VEDTAK.renter is 'Totalt rentebeløp';
comment on column IVERKSATT_VEDTAK.skatt is 'Totalt estimert skattetrekk (differanse mellom brutto_tilbakekreves og netto_tilbakekreves)';
