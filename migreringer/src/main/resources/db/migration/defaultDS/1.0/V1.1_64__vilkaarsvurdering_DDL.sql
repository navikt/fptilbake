CREATE TABLE VILKAAR(
  ID                      NUMBER(19, 0)      NOT NULL,
  OPPRETTET_AV            VARCHAR2(20 CHAR) DEFAULT 'VL'        NOT NULL ,
  OPPRETTET_TID           TIMESTAMP(3) DEFAULT systimestamp      NOT NULL ,
  ENDRET_AV               VARCHAR2(20 CHAR),
  ENDRET_TID              TIMESTAMP(3),
  constraint PK_VILKAAR primary key (id)
);

comment on table VILKAAR is 'Kobler flere perioder av vilkårsvurdering for tilbakekreving';

CREATE TABLE GR_VILKAAR (
  ID                      NUMBER(19, 0)      NOT NULL,
  BEHANDLING_ID           NUMBER(19, 0)      NOT NULL,
  MANUELL_VILKAAR_ID      NUMBER(19, 0)      NOT NULL,
  AKTIV                   varchar2(1 char)   not null,
  OPPRETTET_AV            VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL ,
  OPPRETTET_TID           TIMESTAMP(3) DEFAULT systimestamp      NOT NULL ,
  ENDRET_AV               VARCHAR2(20 CHAR),
  ENDRET_TID              TIMESTAMP(3),
  constraint PK_GR_VILKAAR primary key (id),
  constraint FK_GR_VILKAAR_1 foreign key (MANUELL_VILKAAR_ID) references VILKAAR(ID),
  constraint CHK_AKTIV check (AKTIV in ('J', 'N'))
);

create index IDX_GR_VILKAAR_1 on GR_VILKAAR(BEHANDLING_ID);
create index IDX_GR_VILKAAR_2 on GR_VILKAAR(MANUELL_VILKAAR_ID);

comment on table GR_VILKAAR is 'Versjonering av vilkårsvurdering for tilbakekreving';
comment on column GR_VILKAAR.BEHANDLING_ID is 'Referanse til behandling';
comment on column GR_VILKAAR.MANUELL_VILKAAR_ID is 'Peker på saksbehandlers valg for manuell vilkårvurdering';
comment on column GR_VILKAAR.AKTIV is 'Angir status av manuell vilkårvurdering';


CREATE TABLE VILKAAR_PERIODE(
  ID                      NUMBER(19, 0)        NOT NULL,
  VILKAAR_ID              NUMBER(19, 0)        NOT NULL,
  FOM                     DATE                 NOT NULL,
  TOM                     DATE                 NOT NULL,
  NAV_OPPFULGT            varchar2(100 char)   NOT NULL,
  VILKAAR_RESULTAT        varchar2(100 char)   NOT NULL,
  BEGRUNNELSE             varchar2(1500 char)  NOT NULL,
  KL_NAV_OPPFULGT         generated always as ('NAV_OPPFULGT') virtual,
  KL_VILKAAR_RESULTAT     generated always as ('VILKAAR_RESULTAT') virtual,
  OPPRETTET_AV            VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL ,
  OPPRETTET_TID           TIMESTAMP(3) DEFAULT systimestamp      NOT NULL ,
  ENDRET_AV               VARCHAR2(20 CHAR),
  ENDRET_TID              TIMESTAMP(3),
  constraint PK_VILKAAR_PERIODE primary key (id),
  constraint FK_VILKAAR_PERIODE_1 foreign key (VILKAAR_ID) references VILKAAR(ID),
  constraint FK_VILKAAR_PERIODE_2 foreign key (NAV_OPPFULGT, KL_NAV_OPPFULGT) references KODELISTE(kode, kodeverk),
  constraint FK_VILKAAR_PERIODE_3 foreign key (VILKAAR_RESULTAT, KL_VILKAAR_RESULTAT) references KODELISTE(kode, kodeverk)
);

create index IDX_VILKAAR_PERIODE_1 on VILKAAR_PERIODE(VILKAAR_ID);
create index IDX_VILKAAR_PERIODE_2 on VILKAAR_PERIODE(NAV_OPPFULGT);
create index IDX_VILKAAR_PERIODE_3 on VILKAAR_PERIODE(VILKAAR_RESULTAT);


comment on table VILKAAR_PERIODE is 'Periode med vilkårsvurdering for tilbakekreving';
comment on column VILKAAR_PERIODE.VILKAAR_ID is 'FK:VILKAAR';
comment on column VILKAAR_PERIODE.FOM is 'Fra-og-med-dato';
comment on column VILKAAR_PERIODE.TOM is 'Til-og-med-dato';
comment on column VILKAAR_PERIODE.VILKAAR_RESULTAT is 'Hovedresultat av vilkårsvurdering (kodeverk)';
comment on column VILKAAR_PERIODE.NAV_OPPFULGT is 'Vurdering av hvordan nav har fulgt opp';
comment on column VILKAAR_PERIODE.BEGRUNNELSE is 'Saksbehandlers begrunnelse';

CREATE TABLE VILKAAR_AKTSOMHET(
  ID                        NUMBER(19, 0)        NOT NULL,
  VILKAAR_PERIODE_ID        NUMBER(19, 0)        NOT NULL,
  AKTSOMHET                 varchar2(100 char)   NOT NULL,
  ILEGG_RENTER              varchar2(1 char),
  ANDEL_TILBAKEKREVES       number(3,0),
  BELOEP_TILBAKEKREVES      number(19,0),
  BEGRUNNELSE               varchar2(1500 char)  NOT NULL,
  KL_AKTSOMHET              generated always as ('AKTSOMHET') virtual,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3) DEFAULT systimestamp      NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3),
  constraint PK_VILKAAR_AKTSOMHET primary key (id),
  constraint FK_VILKAAR_AKTSOMHET_1 foreign key (VILKAAR_PERIODE_ID) references VILKAAR_PERIODE(ID),
  constraint FK_VILKAAR_AKTSOMHET_2 foreign key (AKTSOMHET, KL_AKTSOMHET) references KODELISTE(kode, kodeverk),
  constraint CHK_ANDEL_BELOP check ((ANDEL_TILBAKEKREVES is null) or (BELOEP_TILBAKEKREVES is null)),
  constraint CHK_RENTER check (ILEGG_RENTER is null or ILEGG_RENTER in ('J', 'N'))
);

create index IDX_VILKAAR_AKTSOMHET_1 on VILKAAR_AKTSOMHET(VILKAAR_PERIODE_ID);
create index IDX_VILKAAR_AKTSOMHET_2 on VILKAAR_AKTSOMHET(AKTSOMHET);

comment on table VILKAAR_AKTSOMHET is 'Videre vurderinger når det er vurdert at bruker ikke mottok beløp i god tro';
comment on column VILKAAR_AKTSOMHET.VILKAAR_PERIODE_ID is 'FK:VILKAAR_PERIODE';
comment on column VILKAAR_AKTSOMHET.AKTSOMHET is 'Resultat av aktsomhetsvurdering (kodeverk)';
comment on column VILKAAR_AKTSOMHET.ILEGG_RENTER is 'Hvorvidt renter skal ilegges';
comment on column VILKAAR_AKTSOMHET.ANDEL_TILBAKEKREVES is 'Hvor stor del av feilutbetalt beløp som skal tilbakekreves';
comment on column VILKAAR_AKTSOMHET.BELOEP_TILBAKEKREVES is 'Hvor mye av feilutbetalt beløp som skal tilbakekreves';
comment on column VILKAAR_AKTSOMHET.BEGRUNNELSE is 'beskrivelse av aktsomhet';


CREATE TABLE VILKAAR_SAERLIG_GRUNN (
  ID                        NUMBER(19, 0)        NOT NULL,
  VURDER_AKTSOMHET_ID       NUMBER(19, 0)        NOT NULL,
  SAERLIG_GRUNN             varchar2(100 char)   NOT NULL,
  KL_SAERLIG_GRUNN          generated always as ('SAERLIG_GRUNN') virtual,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3) DEFAULT systimestamp      NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3),
  constraint PK_VILKAAR_SAERLIG_GRUNN primary key (id),
  constraint FK_VILKAAR_SAERLIG_GRUNN_1 foreign key (VURDER_AKTSOMHET_ID) references VILKAAR_AKTSOMHET(ID),
  constraint FK_VILKAAR_SAERLIG_GRUNN_2 foreign key (SAERLIG_GRUNN, KL_SAERLIG_GRUNN) references KODELISTE(kode, kodeverk)
);

create index IDX_VILKAAR_SAERLIG_GRUNN_1 ON VILKAAR_SAERLIG_GRUNN (VURDER_AKTSOMHET_ID);
create index IDX_VILKAAR_SAERLIG_GRUNN_2 ON VILKAAR_SAERLIG_GRUNN (SAERLIG_GRUNN);

comment on table VILKAAR_SAERLIG_GRUNN is 'Særlige grunner ved vurdering';
comment on column VILKAAR_SAERLIG_GRUNN.VURDER_AKTSOMHET_ID is 'FK:VILKAAR_AKTSOMHET';
comment on column VILKAAR_SAERLIG_GRUNN.SAERLIG_GRUNN is 'Særlig grunn (kodeverk)';

CREATE TABLE VILKAAR_GOD_TRO (
  ID                        NUMBER(19, 0)      NOT NULL,
  VILKAAR_PERIODE_ID        NUMBER(19, 0)      NOT NULL,
  BELOEP_ER_I_BEHOLD        varchar2(1 char)   NOT NULL,
  BELOEP_TILBAKEKREVES      number(19,0),
  BEGRUNNELSE               varchar2(1500 char) NOT NULL,
  OPPRETTET_AV              VARCHAR2(20 CHAR) DEFAULT 'VL'         NOT NULL,
  OPPRETTET_TID             TIMESTAMP(3) DEFAULT systimestamp      NOT NULL,
  ENDRET_AV                 VARCHAR2(20 CHAR),
  ENDRET_TID                TIMESTAMP(3),
  constraint PK_VILKAAR_GOD_TRO primary key (id),
  constraint FK_VILKAAR_GOD_TRO_1 foreign key (VILKAAR_PERIODE_ID) references VILKAAR_PERIODE(ID)
);

create index IDX_VILKAAR_GOD_TRO ON VILKAAR_GOD_TRO(VILKAAR_PERIODE_ID);

comment on table VILKAAR_GOD_TRO is 'Videre vurderinger når det er vurdert at bruker mottok feilutbetaling i god tro';
comment on column VILKAAR_GOD_TRO.VILKAAR_PERIODE_ID is 'FK:VILKAAR_PERIODE';
comment on column VILKAAR_GOD_TRO.BELOEP_ER_I_BEHOLD is 'Indikerer at beløp er i behold';
comment on column VILKAAR_GOD_TRO.BELOEP_TILBAKEKREVES is 'Hvor mye av feilutbetalt beløp som skal tilbakekreves';
comment on column VILKAAR_GOD_TRO.BEGRUNNELSE is 'beskrivelse av god tro vilkaar';

create sequence SEQ_GR_VILKAAR minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;
create sequence SEQ_VILKAAR minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;
create sequence SEQ_VILKAAR_PERIODE minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;
create sequence SEQ_VILKAAR_AKTSOMHET minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;
create sequence SEQ_VILKAAR_SAERLIG_GRUNN minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;
create sequence SEQ_VILKAAR_GOD_TRO minvalue 1000000 start with 1000000 increment by 50 nocache nocycle;