-- PROSESS TASK
insert into prosess_task_type (kode, navn, feil_maks_forsoek, beskrivelse)
values ('hendelser.håndterHendelse', 'Håndter tilkjent ytelse hendelse', 5, 'Håndter tilkjent ytelse hendelse');

insert into prosess_task_type (kode, navn, feil_maks_forsoek, beskrivelse)
values ('hendelser.opprettBehandling', 'Opprett behandling etter hendelse', 5, 'Opprett behandling etter hendelse');

alter table fagsak_prosess_task drop constraint fk_fagsak_prosess_task_1;
alter table fagsak_prosess_task drop constraint fk_fagsak_prosess_task_3;


-- FAGSAK endringer
drop index idx_fagsak_3;
drop sequence seq_fagsak;

alter table fagsak drop constraint fk_fagsak_3;

alter table fagsak drop column ekstern_fagsak_id;
alter table fagsak drop column ekstern_fagsak_system;
alter table fagsak drop column kl_ekstern_fagsak_system;


-- BEHANDLING endringer
alter table behandling drop column ekstern_behandling_id;


-- EKSTERN_BEHANDLING tabell
create table ekstern_behandling (
  id number(19, 0) not null,
  intern_id number(19, 0) not null,
  ekstern_id number(19, 0) not null,
  aktiv varchar2(1 char) default 'J' not null,
  varseltekst varchar2(2000 char) not null,
  fagsystem varchar2(100 char) not null,
  kl_fagsystem varchar2(100 char) generated always as ('FAGSYSTEM') virtual visible,
  opprettet_av varchar2(20 char) default 'VL' not null,
  opprettet_tid timestamp(3) default sysdate not null,
  endret_av varchar2(20 char),
  endret_tid timestamp(3),
  constraint pk_ekstern_behandling primary key (id),
  constraint fk_ekstern_behandling_1 foreign key (intern_id) references behandling (id),
  constraint fk_ekstern_behandling_2 foreign key (fagsystem, kl_fagsystem) references kodeliste (kode, kodeverk),
  constraint uidx_ekstern_behandling_1 unique (intern_id, ekstern_id)
);

create index idx_ekstern_behandling_1 on ekstern_behandling (intern_id);
create index idx_ekstern_behandling_2 on ekstern_behandling (ekstern_id);
create index idx_ekstern_behandling_3 on ekstern_behandling (fagsystem);

create sequence seq_ekstern_behandling minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;

comment on table ekstern_behandling is 'Referanse til ekstern behandling';
comment on column ekstern_behandling.id is 'Primary key';
comment on column ekstern_behandling.intern_id is 'FK: Behandling Fremmednøkkel for kobling til intern behandling';
comment on column ekstern_behandling.ekstern_id is 'Behandling ID fra eksternt system behandlingen er koblet til';
comment on column ekstern_behandling.aktiv is 'Angir om ekstern behandling data er gjeldende';
comment on column ekstern_behandling.varseltekst is 'Varseltekst saksbehandler har satt i eksternt system';
comment on column ekstern_behandling.fagsystem is 'FK:FAGSYSTEM Fremmednøkkel til kodeverkstabellen som viser hvilket fagsystem behandlingen kommer fra';
comment on column ekstern_behandling.kl_fagsystem is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
