create sequence seq_iverksetting_status_os increment by 50 minvalue 1000050;
create table iverksetting_status_os
(
    id            number(19) not null constraint pk_iverksetting_status_os primary key,
    behandling_id number(19) not null constraint fk_iverksetting_status_os_1 references behandling(id),
    vedtak_id     varchar2(100 char) not null,
    aktiv         varchar2(1 char) default 'J' not null,
    kvittert_ok   varchar2(1 char),
    kvittert_tid  timestamp(3),
    versjon       number(19) default 0 not null,
    opprettet_av  varchar2(20 char) default 'VL' not null,
    opprettet_tid timestamp(3) default systimestamp not null,
    endret_av     varchar2(20 char),
    endret_tid    timestamp(3)
);
comment on table iverksetting_status_os is 'Status for iverksetting mot OS';
comment on column iverksetting_status_os.behandling_id is 'Kobling til behandling';
comment on column iverksetting_status_os.vedtak_id is 'Vedtak id for oppdragsystemet ';
comment on column iverksetting_status_os.kvittert_ok is 'Indikerer om oppdragsystemet godtok vedtaket (J), ikke godtok (N), eller det ikke er vellykket overført (null)';
comment on column iverksetting_status_os.kvittert_tid is 'Viser tidspunktet for når systemet mottok en kvittering fra oppdragsystemet';

create index idx_iverksetting_status_os_1 on iverksetting_status_os (behandling_id);
create index idx_iverksetting_status_os_2 on iverksetting_status_os (opprettet_tid);
create index idx_iverksetting_status_os_3 on iverksetting_status_os (kvittert_tid);
