--------------------------------------------------------
--  DDL for MOTTAKER_VARSEL_RESPONS
--------------------------------------------------------
create table mottaker_varsel_respons (
  id                      number(19, 0)                   not null,
  saksnummer              varchar2(19 char)               not null,
  akseptert_faktagrunnlag varchar2(1 char)                not null,
  opprettet_av            varchar2(200 char) default 'VL' not null,
  opprettet_tid           timestamp(3) default systimestamp,
  endret_av               varchar2(200 char),
  endret_tid              timestamp(3),
  constraint PK_MOTTAKER_VARSEL_RESPONS PRIMARY KEY (id)
);

CREATE SEQUENCE SEQ_MOTTAKER_VARSEL_RESPONS MINVALUE 1 START WITH 100000 INCREMENT BY 50 NOCACHE NOCYCLE;
CREATE UNIQUE INDEX UIDX_MOTTAKER_VARSEL_RESPONS_1 ON mottaker_varsel_respons (saksnummer);

comment on table mottaker_varsel_respons is 'Respons fra mottakere av tbk. varsel';
comment on column mottaker_varsel_respons.id is 'Primary Key';
comment on column mottaker_varsel_respons.saksnummer is 'saksnummeret responsen hører til';
comment on column mottaker_varsel_respons.akseptert_faktagrunnlag is 'Angir om faktagrunnlag har blitt akseptert av bruker';
