create sequence seq_beregningsresultat increment by 50 minvalue 1000050;
create table beregningsresultat
(
id                   number(19) not null constraint pk_beregningsresultat primary key,
vedtak_resultat_type varchar2(50 char) not null,
versjon              number(19) default 0 not null,
opprettet_av         varchar2(20 char) default 'VL' not null,
opprettet_tid        timestamp(3) default systimestamp not null,
endret_av            varchar2(20 char),
endret_tid           timestamp(3)
);
comment on table beregningsresultat is 'Utregnet beregningsresultat';
comment on column beregningsresultat.vedtak_resultat_type is 'Overordnet resultat som kode';

create sequence seq_beregningsresultat_periode increment by 50 minvalue 1000050;
create table beregningsresultat_periode
(
id                            number(19) not null constraint pk_beregningsresultat_periode primary key,
beregningsresultat_id         number(19) not null constraint fk_beregningsresultat_p_1 references beregningsresultat(id),
fom                           date                              not null,
tom                           date                              not null,
er_foreldet                   varchar2(1 char) not null,
tilbakekreving_beloep         number (19, 2) not null,
tilbakekreving_beloep_u_skatt number(19, 2) not null,
tilbakekreving_beloep_u_rente number(19, 2) not null,
renter_prosent                number(19, 2),
renter_beloep                 number(19, 2) not null,
skatt_beloep                  number(19, 2) not null,
feilutbetalt_beloep           number(19, 2) not null,
utbetalt_ytelse_beloep        number(19, 2) not null,
riktig_ytelse_beloep          number(19, 2) not null,

versjon                       number(19) default 0 not null,
opprettet_av                  varchar2(20 char) default 'VL' not null,
opprettet_tid                 timestamp(3) default systimestamp not null,
endret_av                     varchar2(20 char),
endret_tid                    timestamp(3)
);
create index IDX_BEREGNINGSRESULTAT_P_1 on beregningsresultat_periode (beregningsresultat_id);
comment on table beregningsresultat_periode is 'Utregnet beregningsresultat for perioden';
comment on column beregningsresultat_periode.fom is 'Fra-og-med dato for resultatet';
comment on column beregningsresultat_periode.tom is 'Til-og-med dato for resultatet';
comment on column beregningsresultat_periode.er_foreldet is 'Indikerer om perioden var foreldet';
comment on column beregningsresultat_periode.tilbakekreving_beloep is 'Beløp som tilbakekreves, før skatt trekkes fra. Inkluderer evt. renter.';
comment on column beregningsresultat_periode.tilbakekreving_beloep_u_skatt is 'Beløp som tilbakekreves, etter at skatt er trekt fra. Inkluderer evt. renter.';
comment on column beregningsresultat_periode.tilbakekreving_beloep_u_rente is 'Brutto tilbakekrevingsbeløp før renter er lagt til.';
comment on column beregningsresultat_periode.renter_prosent is 'Ilagte renter, prosent';
comment on column beregningsresultat_periode.renter_beloep is 'Ilagte renter, beløp';
comment on column beregningsresultat_periode.skatt_beloep is 'Estimat for skatt som kan trekkes fra tilbakekrevingsbeløpet';
comment on column beregningsresultat_periode.feilutbetalt_beloep is 'Det feilutbetalte beløpet som ble behandlet';
comment on column beregningsresultat_periode.utbetalt_ytelse_beloep is 'Opprinnelig utbetalt ytelse (ikke justert for evt. trekk)';
comment on column beregningsresultat_periode.riktig_ytelse_beloep is 'Riktig ytelse (ikke justert for evt trekk)';

create sequence seq_gr_beregningsresultat increment by 50 minvalue 1000050;
create table gr_beregningsresultat
(
id                    number(19) not null constraint pk_gr_beregningsresultat primary key,
behandling_id         number(19) not null constraint fk_gr_beregningsresultat_1 references behandling(id),
beregningsresultat_id number(19) not null constraint fk_gr_beregningsresultat_2 references beregningsresultat(id),
aktiv                 varchar2(1 char) default 'J' not null,
versjon               number(19) default 0 not null,
opprettet_av          varchar2(20 char) default 'VL' not null,
opprettet_tid         timestamp(3) default systimestamp not null,
endret_av             varchar2(20 char),
endret_tid            timestamp(3)
);
create index IDX_GR_BEREGNINGSRESULTAT_1 on gr_beregningsresultat (behandling_id);
create index IDX_GR_BEREGNINGSRESULTAT_2 on gr_beregningsresultat (beregningsresultat_id);
comment on table gr_beregningsresultat is 'Kobler behandling til beregningsresultat';
comment on column gr_beregningsresultat.behandling_id is 'Kobling til behandling';
comment on column gr_beregningsresultat.beregningsresultat_id is 'Kobling til beregningsresultat';