create sequence SEQ_KODELISTE
    increment by 50
    nocache;

create sequence SEQ_KONFIG_VERDI
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_BRUKER
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING_ARSAK
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_AKSJONSPUNKT
    increment by 50
    nocache;

create sequence SEQ_VURDER_PAA_NYTT_AARSAK
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING_STEG_TILSTAND
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING_TYPE_STEG_SEKV
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING_RESULTAT
    increment by 50
    nocache;

create sequence SEQ_BEHANDLING_VEDTAK
    increment by 50
    nocache;

create sequence SEQ_FAGSAK_PROSESS_TASK
    increment by 50
    nocache;

create sequence SEQ_TOTRINNSVURDERING
    increment by 50
    nocache;

create sequence SEQ_DOKUMENT_ADRESSE
    increment by 50
    nocache;

create sequence SEQ_DOKUMENT_DATA
    increment by 50
    nocache;

create sequence SEQ_DOKUMENT_FELLES
    increment by 50
    nocache;

create sequence SEQ_DOKUMENT_TYPE_DATA
    increment by 50
    nocache;

create sequence SEQ_LAGRET_VEDTAK
    increment by 50
    nocache;

create sequence SEQ_KODELISTE_RELASJON
    increment by 50
    nocache;

create sequence SEQ_PROSESS_TASK
    minvalue 30000000
    increment by 50
    nocache;

create sequence SEQ_PROSESS_TASK_GRUPPE
    minvalue 30000000
    increment by 1000000
    nocache;

create sequence SEQ_VURDER_AARSAK_TTVURDERING
    minvalue 3000000
    increment by 50
    nocache;

create sequence SEQ_KODELISTE_NAVN_I18N
    increment by 50
    nocache;

create sequence SEQ_MOTTAKER_VARSEL_RESPONS
    increment by 50
    nocache;

create sequence SEQ_VURDERT_FORELDELSE
    increment by 50
    nocache;

create sequence SEQ_GR_VURDERT_FORELDELSE
    increment by 50
    nocache;

create sequence SEQ_FORELDELSE_PERIODE
    increment by 50
    nocache;

create sequence SEQ_HISTORIKKINNSLAG
    increment by 50
    nocache;

create sequence SEQ_HISTORIKKINNSLAG_DEL
    increment by 50
    nocache;

create sequence SEQ_HISTORIKKINNSLAG_DOK_LINK
    increment by 50
    nocache;

create sequence SEQ_HISTORIKKINNSLAG_FELT
    increment by 50
    nocache;

create sequence SEQ_FAKTA_FEILUTB_PERIODE
    increment by 50
    nocache;

create sequence SEQ_KRAV_GRUNNLAG_431
    increment by 50
    nocache;

create sequence SEQ_KRAV_GRUNNLAG_PERIODE_432
    increment by 50
    nocache;

create sequence SEQ_KRAV_GRUNNLAG_BELOP_433
    increment by 50
    nocache;

create sequence SEQ_KRAV_VEDTAK_STATUS_437
    increment by 50
    nocache;

create sequence SEQ_GR_KRAV_GRUNNLAG
    increment by 50
    nocache;

create sequence SEQ_GR_VILKAAR
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VILKAAR
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VILKAAR_PERIODE
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VILKAAR_AKTSOMHET
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VILKAAR_SAERLIG_GRUNN
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VILKAAR_GOD_TRO
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_EKSTERN_BEHANDLING
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_FAKTA_FEILUTBETALING
    increment by 50
    nocache;

create sequence SEQ_GR_FAKTA_FEILUTBETALING
    increment by 50
    nocache;

create sequence SEQ_OKO_XML_MOTTATT
    increment by 50
    nocache;

create sequence SEQ_TOTRINNRESULTATGRUNNLAG
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VARSELBREV_DATA
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VEDTAKSBREV_SPORING
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VEDTAKSBREV_OPPSUMMERING
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VEDTAKSBREV_PERIODE
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_OKO_XML_SENDT
    increment by 50
    nocache;

create sequence SEQ_FAGSAK
    increment by 50
    nocache;

create sequence SEQ_VARSEL
    increment by 50
    nocache;

create sequence SEQ_GR_KRAV_VEDTAK_STATUS
    increment by 50
    nocache;

create sequence SEQ_BREV_SPORING
    minvalue 1000000
    increment by 50
    nocache;

create sequence SEQ_VERGE
    increment by 50
    nocache;

create sequence SEQ_GR_VERGE
    increment by 50
    nocache;

create table BRUKER
(
    ID            NUMBER(19)                              not null
        constraint PK_BRUKER
        primary key,
    AKTOER_ID     VARCHAR2(50 char),
    SPRAK_KODE    VARCHAR2(100 char) default 'NB'         not null,
    VERSJON       NUMBER(19)         default 0            not null,
    OPPRETTET_AV  VARCHAR2(20 char)  default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3)
);

comment on table BRUKER is 'Bruker som saken gjelder.';

comment on column BRUKER.ID is 'Primary Key';

comment on column BRUKER.AKTOER_ID is 'Aktørid utstedt av Nav Aktørregister for en Bruker (eks. Søker)';

comment on column BRUKER.SPRAK_KODE is 'FK:SPRAAK_KODE Fremmednøkkel til kodeverkstabellen som viser språk som er støttet og viser til brukerens foretrukne språk';

create table FAGSAK
(
    ID            NUMBER(19)                              not null
        constraint PK_FAGSAK
        primary key,
    SAKSNUMMER    VARCHAR2(50 char),
    FAGSAK_STATUS VARCHAR2(100 char)                      not null,
    BRUKER_ID     NUMBER(19)                              not null
        constraint FK_FAGSAK_1
        references BRUKER,
    VERSJON       NUMBER(19)         default 0            not null,
    OPPRETTET_AV  VARCHAR2(20 char)  default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    YTELSE_TYPE   VARCHAR2(100 char) default 'FP'         not null
);

comment on table FAGSAK is 'Fagsak for tilbakekreving. Alle behandling er koblet mot en fagsak.';

comment on column FAGSAK.ID is 'Primary Key';

comment on column FAGSAK.SAKSNUMMER is 'Saksnummer (som GSAK har mottatt)';

comment on column FAGSAK.FAGSAK_STATUS is 'FK:FAGSAK_STATUS Fremmednøkkel til kodeverkstabellen som inneholder oversikten over fagstatuser';

comment on column FAGSAK.BRUKER_ID is 'FK:BRUKER Fremmednøkkel til brukertabellen';

comment on column FAGSAK.YTELSE_TYPE is 'Fremmednøkkel til kodeverkstabellen som inneholder oversikt over ytelser';

create unique index UIDX_FAGSAK_1
    on FAGSAK (SAKSNUMMER);

create index IDX_FAGSAK_1
    on FAGSAK (FAGSAK_STATUS);

create index IDX_FAGSAK_2
    on FAGSAK (BRUKER_ID);

create index IDX_FAGSAK_3
    on FAGSAK (YTELSE_TYPE);

create table BEHANDLING
(
    ID                      NUMBER(19)                             not null
        constraint PK_BEHANDLING
        primary key,
    FAGSAK_ID               NUMBER(19)                             not null
        constraint FK_BEHANDLING_1
        references FAGSAK,
    BEHANDLING_STATUS       VARCHAR2(100 char)                     not null,
    BEHANDLING_TYPE         VARCHAR2(100 char)                     not null,
    OPPRETTET_DATO          DATE              default sysdate      not null,
    AVSLUTTET_DATO          DATE,
    ANSVARLIG_SAKSBEHANDLER VARCHAR2(100 char),
    ANSVARLIG_BESLUTTER     VARCHAR2(100 char),
    BEHANDLENDE_ENHET       VARCHAR2(10 char),
    BEHANDLENDE_ENHET_NAVN  VARCHAR2(320 char),
    VERSJON                 NUMBER(19)        default 0            not null,
    OPPRETTET_AV            VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID           TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3),
    MANUELT_OPPRETTET       VARCHAR2(1)                            not null,
    UUID                    RAW(16)                                not null,
    SAKSBEHANDLING_TYPE     VARCHAR2(50 char)                      not null
        constraint CHK_SAKSBEHANDLING_TYPE
        check (SAKSBEHANDLING_TYPE in ('ORDINÆR', 'AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP'))
);

comment on table BEHANDLING is 'Behandling av fagsak';

comment on column BEHANDLING.ID is 'Primary Key';

comment on column BEHANDLING.FAGSAK_ID is 'FK: FAGSAK Fremmednøkkel for kobling til fagsak';

comment on column BEHANDLING.BEHANDLING_STATUS is 'FK: BEHANDLING_STATUS Fremmednøkkel til tabellen som viser status på behandlinger';

comment on column BEHANDLING.BEHANDLING_TYPE is 'FK: BEHANDLING_TYPE Fremmedøkkel til oversikten over hvilken behandlingstyper som finnes';

comment on column BEHANDLING.OPPRETTET_DATO is 'Dato når behandlingen ble opprettet.';

comment on column BEHANDLING.AVSLUTTET_DATO is 'Dato når behandlingen ble avsluttet.';

comment on column BEHANDLING.ANSVARLIG_SAKSBEHANDLER is 'Id til saksbehandler som oppretter forslag til vedtak ved totrinnsbehandling.';

comment on column BEHANDLING.ANSVARLIG_BESLUTTER is 'Beslutter som har fattet vedtaket';

comment on column BEHANDLING.BEHANDLENDE_ENHET is 'NAV-enhet som behandler behandlingen';

comment on column BEHANDLING.BEHANDLENDE_ENHET_NAVN is 'Navn på behandlende enhet';

comment on column BEHANDLING.MANUELT_OPPRETTET is 'Angir om behandlingen ble opprettet manuelt. ';

comment on column BEHANDLING.UUID is 'Unik UUID for behandling til utvortes bruk';

comment on column BEHANDLING.SAKSBEHANDLING_TYPE is 'Angir hvordan behandlingen saksbehandles ';

create index IDX_BEHANDLING_1
    on BEHANDLING (FAGSAK_ID);

create index IDX_BEHANDLING_2
    on BEHANDLING (BEHANDLING_STATUS);

create index IDX_BEHANDLING_3
    on BEHANDLING (BEHANDLING_TYPE);

create unique index UIDX_BEHANDLING_03
    on BEHANDLING (UUID);

create table BEHANDLING_ARSAK
(
    ID                     NUMBER(19)                             not null
        constraint PK_BEHANDLING_ARSAK
        primary key,
    BEHANDLING_ID          NUMBER(19)                             not null
        constraint FK_BEHANDLING_ARSAK_1
        references BEHANDLING,
    BEHANDLING_ARSAK_TYPE  VARCHAR2(35 char)                      not null,
    VERSJON                NUMBER(19)        default 0            not null,
    OPPRETTET_AV           VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID          TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV              VARCHAR2(20 char),
    ENDRET_TID             TIMESTAMP(3),
    ORIGINAL_BEHANDLING_ID NUMBER(19)
        constraint FK_BEHANDLING_ARSAK_2
        references BEHANDLING
);

comment on table BEHANDLING_ARSAK is 'Årsak for rebehandling';

comment on column BEHANDLING_ARSAK.ID is 'Primary Key';

comment on column BEHANDLING_ARSAK.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling';

comment on column BEHANDLING_ARSAK.BEHANDLING_ARSAK_TYPE is 'FK: BEHANDLING_ARSAK_TYPE Fremmednøkkel til oversikten over hvilke årsaker en behandling kan begrunnes med';

comment on column BEHANDLING_ARSAK.ORIGINAL_BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandlingen denne raden i tabellen hører til';

create index IDX_BEHANDLING_ARSAK_1
    on BEHANDLING_ARSAK (BEHANDLING_ID);

create index IDX_BEHANDLING_ARSAK_2
    on BEHANDLING_ARSAK (ORIGINAL_BEHANDLING_ID);

create table AKSJONSPUNKT
(
    ID                     NUMBER(19)                              not null
        constraint PK_AKSJONSPUNKT
        primary key,
    TOTRINN_BEHANDLING     VARCHAR2(1 char)                        not null
        constraint CHK_TOTRINNSBEHANDLING
        check (TOTRINN_BEHANDLING = 'J' OR TOTRINN_BEHANDLING = 'N'),
    BEHANDLING_STEG_FUNNET VARCHAR2(100 char),
    AKSJONSPUNKT_STATUS    VARCHAR2(100 char)                      not null,
    AKSJONSPUNKT_DEF       VARCHAR2(50 char)                       not null,
    FRIST_TID              TIMESTAMP(3),
    VENT_AARSAK            VARCHAR2(100 char) default '-'          not null,
    BEHANDLING_ID          NUMBER(19)                              not null
        constraint FK_AKSJONSPUNKT_2
        references BEHANDLING,
    VERSJON                NUMBER(19)         default 0            not null,
    OPPRETTET_AV           VARCHAR2(20 char)  default 'VL'         not null,
    OPPRETTET_TID          TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV              VARCHAR2(20 char),
    ENDRET_TID             TIMESTAMP(3)
);

comment on table AKSJONSPUNKT is 'Aksjoner som en saksbehandler må utføre manuelt.';

comment on column AKSJONSPUNKT.ID is 'Primary Key';

comment on column AKSJONSPUNKT.TOTRINN_BEHANDLING is 'Indikerer at aksjonspunkter krever en totrinnsbehandling';

comment on column AKSJONSPUNKT.BEHANDLING_STEG_FUNNET is 'Hvilket steg ble dette aksjonspunktet funnet i?';

comment on column AKSJONSPUNKT.AKSJONSPUNKT_STATUS is 'FK:AKSJONSPUNKT_STATUS Fremmednøkkel til tabellen som inneholder status på aksjonspunktene';

comment on column AKSJONSPUNKT.AKSJONSPUNKT_DEF is 'FK:AKSJONSPUNKT_DEF Fremmednøkkel til tabellen som inneholder definisjonene av aksjonspunktene';

comment on column AKSJONSPUNKT.FRIST_TID is 'Behandling blir automatisk gjenopptatt etter dette tidspunktet';

comment on column AKSJONSPUNKT.VENT_AARSAK is 'Årsak for at behandling er satt på vent';

comment on column AKSJONSPUNKT.BEHANDLING_ID is 'Fremmednøkkel for kobling til behandling';

create unique index IDX_AKSJONSPUNKT_1
    on AKSJONSPUNKT (BEHANDLING_ID, AKSJONSPUNKT_DEF);

create index IDX_AKSJONSPUNKT_2
    on AKSJONSPUNKT (BEHANDLING_STEG_FUNNET);

create index IDX_AKSJONSPUNKT_3
    on AKSJONSPUNKT (AKSJONSPUNKT_DEF);

create index IDX_AKSJONSPUNKT_4
    on AKSJONSPUNKT (VENT_AARSAK);

create index IDX_AKSJONSPUNKT_5
    on AKSJONSPUNKT (AKSJONSPUNKT_STATUS);

alter table AKSJONSPUNKT
    add constraint CHK_UNIQUE_BEH_AD
        unique (BEHANDLING_ID, AKSJONSPUNKT_DEF);

create table VURDER_PAA_NYTT_AARSAK
(
    ID              NUMBER(19)                             not null
        constraint PK_VURDER_PAA_NYTT_AARSAK
        primary key,
    AKSJONSPUNKT_ID NUMBER(19)                             not null
        constraint FK_VURDER_PAA_NYTT_AARSAK_1
        references AKSJONSPUNKT,
    AARSAK_TYPE     VARCHAR2(100 char)                     not null,
    OPPRETTET_AV    VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID   TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV       VARCHAR2(20 char),
    ENDRET_TID      TIMESTAMP(3)
);

comment on table VURDER_PAA_NYTT_AARSAK is 'Årsaken til at aksjonspunkt må vurderes på nytt';

comment on column VURDER_PAA_NYTT_AARSAK.ID is 'Primary Key';

comment on column VURDER_PAA_NYTT_AARSAK.AKSJONSPUNKT_ID is 'FK:AKSJONSPUNKT Fremmednøkkel til aksjonspunktet som må vurderes på nytt';

comment on column VURDER_PAA_NYTT_AARSAK.AARSAK_TYPE is 'Årsak for at aksjonspunkt må vurders på nytt';

create index IDX_VURDER_PAA_NYTT_AARSAK_1
    on VURDER_PAA_NYTT_AARSAK (AKSJONSPUNKT_ID);

create index IDX_VURDER_PAA_NYTT_AARSAK_2
    on VURDER_PAA_NYTT_AARSAK (AARSAK_TYPE);

create table BEHANDLING_STEG_TILSTAND
(
    ID                     NUMBER(19)                             not null
        constraint PK_BEHANDLING_STEG_TILSTAND
        primary key,
    BEHANDLING_ID          NUMBER(19)                             not null
        constraint FK_BEHANDLING_STEG_TILSTAND_1
        references BEHANDLING,
    BEHANDLING_STEG        VARCHAR2(100 char)                     not null,
    BEHANDLING_STEG_STATUS VARCHAR2(100 char)                     not null,
    VERSJON                NUMBER(19)        default 0            not null,
    OPPRETTET_AV           VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID          TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV              VARCHAR2(20 char),
    ENDRET_TID             TIMESTAMP(3)
);

comment on table BEHANDLING_STEG_TILSTAND is 'Angir tilstand for behandlingsteg som kjøres';

comment on column BEHANDLING_STEG_TILSTAND.ID is 'Primary Key';

comment on column BEHANDLING_STEG_TILSTAND.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandlingen dette steget er tilknyttet';

comment on column BEHANDLING_STEG_TILSTAND.BEHANDLING_STEG is 'Hvilket BehandlingSteg som kjøres';

comment on column BEHANDLING_STEG_TILSTAND.BEHANDLING_STEG_STATUS is 'Status på steg: (ved) INNGANG, STARTET, VENTER, (ved) UTGANG, UTFØRT';

create index IDX_BEHANDLING_STEG_TILSTAND_1
    on BEHANDLING_STEG_TILSTAND (BEHANDLING_ID);

create index IDX_BEHANDLING_STEG_TILSTAND_2
    on BEHANDLING_STEG_TILSTAND (BEHANDLING_STEG_STATUS);

create index IDX_BEHANDLING_STEG_TILSTAND_3
    on BEHANDLING_STEG_TILSTAND (BEHANDLING_STEG);

create table BEHANDLING_RESULTAT
(
    ID                       NUMBER(19)                                 not null
        constraint PK_BEHANDLING_RESULTAT
        primary key,
    BEHANDLING_ID            NUMBER(19)                                 not null
        constraint FK_BEHANDLING_RESULTAT_3
        references BEHANDLING,
    VERSJON                  NUMBER(19)         default 0               not null,
    OPPRETTET_AV             VARCHAR2(20 char)  default 'VL'            not null,
    OPPRETTET_TID            TIMESTAMP(3)       default systimestamp    not null,
    ENDRET_AV                VARCHAR2(20 char),
    ENDRET_TID               TIMESTAMP(3),
    BEHANDLING_RESULTAT_TYPE VARCHAR2(100 char) default 'IKKE_FASTSATT' not null
);

comment on table BEHANDLING_RESULTAT is 'Beregningsresultat. Knytter sammen beregning og behandling.';

comment on column BEHANDLING_RESULTAT.ID is 'Primary Key';

comment on column BEHANDLING_RESULTAT.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling';

comment on column BEHANDLING_RESULTAT.BEHANDLING_RESULTAT_TYPE is 'Resultat av behandlingen';

create index IDX_BEHANDLING_RESULTAT_1
    on BEHANDLING_RESULTAT (BEHANDLING_ID);

create index IDX_BEHANDLING_RESULTAT_2
    on BEHANDLING_RESULTAT (BEHANDLING_RESULTAT_TYPE);

create table BEHANDLING_VEDTAK
(
    ID                      NUMBER(19)                                  not null
        constraint PK_BEHANDLING_VEDTAK
        primary key,
    VEDTAK_DATO             DATE                                        not null,
    ANSVARLIG_SAKSBEHANDLER VARCHAR2(40 char)                           not null,
    BEHANDLING_RESULTAT_ID  NUMBER(19)                                  not null
        constraint FK_BEHANDLING_VEDTAK_1
        references BEHANDLING_RESULTAT,
    VERSJON                 NUMBER(19)         default 0                not null,
    OPPRETTET_AV            VARCHAR2(20 char)  default 'VL'             not null,
    OPPRETTET_TID           TIMESTAMP(3)       default systimestamp     not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3),
    IVERKSETTING_STATUS     VARCHAR2(100 char) default 'IKKE_IVERKSATT' not null
);

comment on table BEHANDLING_VEDTAK is 'Vedtak koblet til en behandling via et behandlingsresultat.';

comment on column BEHANDLING_VEDTAK.ID is 'Primary Key';

comment on column BEHANDLING_VEDTAK.VEDTAK_DATO is 'Vedtaksdato.';

comment on column BEHANDLING_VEDTAK.ANSVARLIG_SAKSBEHANDLER is 'Ansvarlig saksbehandler som godkjente vedtaket.';

comment on column BEHANDLING_VEDTAK.BEHANDLING_RESULTAT_ID is 'FK:BEHANDLING_RESULTAT Fremmednøkkel til tabellen som viser behandlingsresultatet';

comment on column BEHANDLING_VEDTAK.IVERKSETTING_STATUS is 'Status for iverksettingssteget';

create unique index UIDX_BEHANDLING_VEDTAK_1
    on BEHANDLING_VEDTAK (BEHANDLING_RESULTAT_ID);

create index IDX_VEDTAK_2
    on BEHANDLING_VEDTAK (ANSVARLIG_SAKSBEHANDLER);

create index IDX_VEDTAK_3
    on BEHANDLING_VEDTAK (VEDTAK_DATO);

create index IDX_BEHANDLING_VEDTAK_1
    on BEHANDLING_VEDTAK (IVERKSETTING_STATUS);

create table PROSESS_TASK
(
    ID                        NUMBER(19)                              not null
        constraint PK_PROSESS_TASK
        primary key,
    TASK_TYPE                 VARCHAR2(50 char)                       not null,
    PRIORITET                 NUMBER(3)          default 0            not null,
    STATUS                    VARCHAR2(20 char)  default 'KLAR'       not null
        constraint CHK_PROSESS_TASK_STATUS
        check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT')),
    TASK_PARAMETERE           VARCHAR2(4000 char),
    TASK_PAYLOAD              CLOB,
    TASK_GRUPPE               VARCHAR2(250 char),
    TASK_SEKVENS              VARCHAR2(100 char) default '1'          not null,
    NESTE_KJOERING_ETTER      TIMESTAMP(0)       default current_timestamp,
    FEILEDE_FORSOEK           NUMBER(5)          default 0,
    SISTE_KJOERING_TS         TIMESTAMP(6),
    SISTE_KJOERING_FEIL_KODE  VARCHAR2(50 char),
    SISTE_KJOERING_FEIL_TEKST CLOB,
    SISTE_KJOERING_SERVER     VARCHAR2(50 char),
    VERSJON                   NUMBER(19)         default 0            not null,
    OPPRETTET_AV              VARCHAR2(20 char)  default 'VL'         not null,
    OPPRETTET_TID             TIMESTAMP(6)       default systimestamp not null,
    BLOKKERT_AV               NUMBER(19),
    SISTE_KJOERING_PLUKK_TS   TIMESTAMP(6),
    SISTE_KJOERING_SLUTT_TS   TIMESTAMP(6)
);

comment on table PROSESS_TASK is 'Inneholder tasks som skal kjøres i bakgrunnen';

comment on column PROSESS_TASK.ID is 'Primary Key';

comment on column PROSESS_TASK.TASK_TYPE is 'navn på task. Brukes til å matche riktig implementasjon';

comment on column PROSESS_TASK.PRIORITET is 'prioritet på task.  Høyere tall har høyere prioritet';

comment on column PROSESS_TASK.STATUS is 'status på task: KLAR, NYTT_FORSOEK, FEILET, VENTER_SVAR, FERDIG';

comment on column PROSESS_TASK.TASK_PARAMETERE is 'parametere angitt for en task';

comment on column PROSESS_TASK.TASK_PAYLOAD is 'inputdata for en task';

comment on column PROSESS_TASK.TASK_GRUPPE is 'angir en unik id som grupperer flere ';

comment on column PROSESS_TASK.TASK_SEKVENS is 'angir rekkefølge på task innenfor en gruppe ';

comment on column PROSESS_TASK.NESTE_KJOERING_ETTER is 'tasken skal ikke kjøeres før tidspunkt er passert';

comment on column PROSESS_TASK.FEILEDE_FORSOEK is 'antall feilede forsøk';

comment on column PROSESS_TASK.SISTE_KJOERING_TS is 'siste gang tasken ble forsøkt kjørt (før kjøring)';

comment on column PROSESS_TASK.SISTE_KJOERING_FEIL_KODE is 'siste feilkode tasken fikk';

comment on column PROSESS_TASK.SISTE_KJOERING_FEIL_TEKST is 'siste feil tasken fikk';

comment on column PROSESS_TASK.SISTE_KJOERING_SERVER is 'navn på node som sist kjørte en task (server@pid)';

comment on column PROSESS_TASK.VERSJON is 'angir versjon for optimistisk låsing';

comment on column PROSESS_TASK.BLOKKERT_AV is 'Id til ProsessTask som blokkerer kjøring av denne (når status=VETO)';

comment on column PROSESS_TASK.SISTE_KJOERING_PLUKK_TS is 'siste gang tasken ble forsøkt plukket (fra db til in-memory, før kjøring)';

comment on column PROSESS_TASK.SISTE_KJOERING_SLUTT_TS is 'tidsstempel siste gang tasken ble kjørt (etter kjøring)';

create index IDX_PROSESS_TASK_1
    on PROSESS_TASK (STATUS);

create index IDX_PROSESS_TASK_2
    on PROSESS_TASK (TASK_TYPE);

create index IDX_PROSESS_TASK_3
    on PROSESS_TASK (NESTE_KJOERING_ETTER);

create index IDX_PROSESS_TASK_4
    on PROSESS_TASK (TASK_GRUPPE);

create index IDX_PROSESS_TASK_6
    on PROSESS_TASK (BLOKKERT_AV);

create table FAGSAK_PROSESS_TASK
(
    ID               NUMBER(19)                             not null
        constraint PK_FAGSAK_PROSESS_TASK
        primary key,
    FAGSAK_ID        NUMBER(19)                             not null,
    PROSESS_TASK_ID  NUMBER(19)                             not null
        constraint FK_FAGSAK_PROSESS_TASK_2
        references PROSESS_TASK,
    BEHANDLING_ID    NUMBER(19),
    VERSJON          NUMBER(19)        default 0            not null,
    OPPRETTET_AV     VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID    TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV        VARCHAR2(20 char),
    ENDRET_TID       TIMESTAMP(3),
    GRUPPE_SEKVENSNR NUMBER(19)
);

comment on table FAGSAK_PROSESS_TASK is '1-M relasjonstabell for ? mappe fagsak til prosess_tasks (som ikke er FERDIG)';

comment on column FAGSAK_PROSESS_TASK.ID is 'Primærnøkkel';

comment on column FAGSAK_PROSESS_TASK.FAGSAK_ID is 'FK: Fremmednøkkel for kobling til fagsak';

comment on column FAGSAK_PROSESS_TASK.PROSESS_TASK_ID is 'FK: Fremmednøkkel for knyttning til logging av prosesstask som ???';

comment on column FAGSAK_PROSESS_TASK.BEHANDLING_ID is 'FK: Fremmednøkkel for kobling til behandling';

comment on column FAGSAK_PROSESS_TASK.GRUPPE_SEKVENSNR is 'For en gitt fagsak angir hvilken rekkefølge task skal kjøres.  Kun tasks med laveste gruppe_sekvensnr vil kjøres. Når disse er FERDIG vil de ryddes bort og neste med lavest sekvensnr kan kjøres (gitt at den er KLAR)';

create unique index UIDX_FAGSAK_PROSESS_TASK_1
    on FAGSAK_PROSESS_TASK (FAGSAK_ID, PROSESS_TASK_ID);

create index IDX_FAGSAK_PROSESS_TASK_1
    on FAGSAK_PROSESS_TASK (FAGSAK_ID);

create index IDX_FAGSAK_PROSESS_TASK_2
    on FAGSAK_PROSESS_TASK (PROSESS_TASK_ID);

create index IDX_FAGSAK_PROSESS_TASK_3
    on FAGSAK_PROSESS_TASK (BEHANDLING_ID);

create index IDX_FAGSAK_PROSESS_TASK_4
    on FAGSAK_PROSESS_TASK (GRUPPE_SEKVENSNR);

create table TOTRINNSVURDERING
(
    ID               NUMBER(19)                             not null
        constraint PK_TOTRINNSVURDERING
        primary key,
    BEHANDLING_ID    NUMBER(19)                             not null
        constraint FK_TOTRINNSVURDERING_2
        references BEHANDLING,
    AKSJONSPUNKT_DEF VARCHAR2(50 char)                      not null,
    AKTIV            VARCHAR2(1 char)  default 'J'          not null,
    GODKJENT         VARCHAR2(1 char)                       not null,
    BEGRUNNELSE      VARCHAR2(4000 char),
    VERSJON          NUMBER(19)        default 0            not null,
    OPPRETTET_AV     VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID    TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV        VARCHAR2(20 char),
    ENDRET_TID       TIMESTAMP(3)
);

comment on table TOTRINNSVURDERING is 'Statisk read only totrinnsvurdering som brukes til å vise vurderinger til aksjonspunkter uavhengig av status';

comment on column TOTRINNSVURDERING.AKSJONSPUNKT_DEF is 'Kodeverdi for definisjon av aksjonspunkt';

comment on column TOTRINNSVURDERING.GODKJENT is 'Beslutters godkjenning';

comment on column TOTRINNSVURDERING.BEGRUNNELSE is 'Beslutters begrunnelse';

create index IDX_TOTRINNSVURDERING_1
    on TOTRINNSVURDERING (AKSJONSPUNKT_DEF);

create index IDX_TOTRINNSVURDERING_2
    on TOTRINNSVURDERING (BEHANDLING_ID);

create table VURDER_AARSAK_TTVURDERING
(
    ID                   NUMBER(19)                             not null
        constraint PK_VURDER_AARSAK_TTVURDERING
        primary key,
    AARSAK_TYPE          VARCHAR2(100 char)                     not null,
    TOTRINNSVURDERING_ID NUMBER(19)                             not null
        constraint FK_VURDER_AARSAK_TTVURDERING_1
        references TOTRINNSVURDERING,
    OPPRETTET_AV         VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID        TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV            VARCHAR2(20 char),
    ENDRET_TID           TIMESTAMP(3)
);

comment on table VURDER_AARSAK_TTVURDERING is 'Årsaken til at aksjonspunkt må vurderes på nytt';

comment on column VURDER_AARSAK_TTVURDERING.AARSAK_TYPE is 'Årsak til at løsning på aksjonspunkt er underkjent';

create index IDX_VURDER_AARSAK
    on VURDER_AARSAK_TTVURDERING (TOTRINNSVURDERING_ID);

create index IDX_VURDER_AARSAK_2
    on VURDER_AARSAK_TTVURDERING (AARSAK_TYPE);

create table MOTTAKER_VARSEL_RESPONS
(
    ID                      NUMBER(19)                      not null
        constraint PK_MOTTAKER_VARSEL_RESPONS
        primary key,
    BEHANDLING_ID           NUMBER(19)                      not null
        constraint FK_MOTTAKER_VARSEL_RESPONS_1
        references BEHANDLING,
    AKSEPTERT_FAKTAGRUNNLAG VARCHAR2(1 char),
    OPPRETTET_AV            VARCHAR2(200 char) default 'VL' not null,
    OPPRETTET_TID           TIMESTAMP(3)       default systimestamp,
    ENDRET_AV               VARCHAR2(200 char),
    ENDRET_TID              TIMESTAMP(3),
    KILDE                   VARCHAR2(100 char)              not null,
    VERSJON                 NUMBER(19)         default 0    not null
);

comment on table MOTTAKER_VARSEL_RESPONS is 'Respons fra mottakere av tbk. varsel';

comment on column MOTTAKER_VARSEL_RESPONS.ID is 'Primary Key';

comment on column MOTTAKER_VARSEL_RESPONS.BEHANDLING_ID is 'behandlingen responsen hører til';

comment on column MOTTAKER_VARSEL_RESPONS.AKSEPTERT_FAKTAGRUNNLAG is 'Angir om faktagrunnlag har blitt akseptert av bruker';

comment on column MOTTAKER_VARSEL_RESPONS.KILDE is 'Angir hvor responsen ble registrert';

create unique index UIDX_MOTTAKER_VARSEL_RESPONS_1
    on MOTTAKER_VARSEL_RESPONS (BEHANDLING_ID);

create table VURDERT_FORELDELSE
(
    ID            NUMBER(19)                              not null
        constraint PK_VURDERT_FORELDELSE
        primary key,
    OPPRETTET_AV  VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3)
);

comment on table VURDERT_FORELDELSE is 'Tabell for å lagre vurdert foreldelse';

comment on column VURDERT_FORELDELSE.ID is 'Primary Key';

create table GR_VURDERT_FORELDELSE
(
    ID                    NUMBER(19)                              not null
        constraint PK_GR_VURDERT_FORELDELSE
        primary key,
    VURDERT_FORELDELSE_ID NUMBER(19)                              not null
        constraint FK_GR_VURDERT_FORELDELSE_01
        references VURDERT_FORELDELSE,
    BEHANDLING_ID         NUMBER(19)                              not null,
    AKTIV                 VARCHAR2(1)        default 'J'          not null,
    OPPRETTET_AV          VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID         TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV             VARCHAR2(20 char),
    ENDRET_TID            TIMESTAMP(3),
    VERSJON               NUMBER(19)         default 0            not null
);

comment on table GR_VURDERT_FORELDELSE is 'Aggregate tabell for å lagre vurdert foreldelse';

comment on column GR_VURDERT_FORELDELSE.ID is 'Primary Key';

comment on column GR_VURDERT_FORELDELSE.VURDERT_FORELDELSE_ID is 'FK:VURDERT_FORELDELSE';

comment on column GR_VURDERT_FORELDELSE.BEHANDLING_ID is 'FK: BEHANDLING fremmednøkkel for tilknyttet behandling';

comment on column GR_VURDERT_FORELDELSE.AKTIV is 'Angir status av vurdert foreldelse';

create index IDX_GR_VURDERT_FORELDELSE_1
    on GR_VURDERT_FORELDELSE (VURDERT_FORELDELSE_ID);

create table FORELDELSE_PERIODE
(
    ID                        NUMBER(19)                              not null
        constraint PK_FORELDELSE_PERIODE
        primary key,
    VURDERT_FORELDELSE_ID     NUMBER(19)                              not null
        constraint FK_FORELDELSE_PERIODE_1
        references VURDERT_FORELDELSE,
    FOM                       DATE                                    not null,
    TOM                       DATE                                    not null,
    FORELDELSE_VURDERING_TYPE VARCHAR2(100 char)                      not null,
    OPPRETTET_AV              VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID             TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV                 VARCHAR2(20 char),
    ENDRET_TID                TIMESTAMP(3),
    BEGRUNNELSE               VARCHAR2(4000 char)                     not null,
    FORELDELSESFRIST          DATE,
    OPPDAGELSES_DATO          DATE
);

comment on table FORELDELSE_PERIODE is 'Tabell for å lagre ny utbetaling periode opprettet av saksbehandler';

comment on column FORELDELSE_PERIODE.ID is 'Primary Key';

comment on column FORELDELSE_PERIODE.VURDERT_FORELDELSE_ID is 'FK:VURDERT_FORELDELSE';

comment on column FORELDELSE_PERIODE.FOM is 'Første dag av ny utbetaling periode';

comment on column FORELDELSE_PERIODE.TOM is 'Siste dag av ny utbetaling periode';

comment on column FORELDELSE_PERIODE.FORELDELSE_VURDERING_TYPE is 'Foreldelse vurdering type av en periode';

comment on column FORELDELSE_PERIODE.BEGRUNNELSE is 'Begrunnelse for endre periode';

comment on column FORELDELSE_PERIODE.FORELDELSESFRIST is 'Foreldelsesfrist for når feilutbetalingen kan innkreves';

comment on column FORELDELSE_PERIODE.OPPDAGELSES_DATO is 'Dato for når feilutbetalingen ble oppdaget';

create index IDX_FORELDELSE_PERIODE_1
    on FORELDELSE_PERIODE (VURDERT_FORELDELSE_ID);

create index IDX_FORELDELSE_PERIODE_2
    on FORELDELSE_PERIODE (FORELDELSE_VURDERING_TYPE);

create table HISTORIKKINNSLAG
(
    ID                    NUMBER(19)                             not null
        constraint PK_HISTORIKKINNSLAG
        primary key,
    TEKST                 VARCHAR2(2000 char),
    BEHANDLING_ID         NUMBER(19)
        constraint FK_HISTORIKKINNSLAG_1
        references BEHANDLING,
    HISTORIKK_AKTOER_ID   VARCHAR2(100 char)                     not null,
    HISTORIKKINNSLAG_TYPE VARCHAR2(100 char)                     not null,
    OPPRETTET_AV          VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID         TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV             VARCHAR2(20 char),
    ENDRET_TID            TIMESTAMP(3),
    FAGSAK_ID             NUMBER(19)                             not null
        constraint FK_HISTORIKKINNSLAG_3
        references FAGSAK,
    BRUKER_KJOENN         VARCHAR2(100 char)                     not null
);

comment on table HISTORIKKINNSLAG is 'Historikk over hendelser i saken';

comment on column HISTORIKKINNSLAG.ID is 'Primary Key';

comment on column HISTORIKKINNSLAG.TEKST is 'Tekst som beskriver hendelsen (som skal vises i historikkfanen)';

comment on column HISTORIKKINNSLAG.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling';

comment on column HISTORIKKINNSLAG.HISTORIKK_AKTOER_ID is 'FK:HISTORIKK_AKTOER Fremmednøkkel til';

comment on column HISTORIKKINNSLAG.HISTORIKKINNSLAG_TYPE is 'Fremmednøkkel til beskrivelse av historikkinnslaget';

comment on column HISTORIKKINNSLAG.FAGSAK_ID is 'FK:FAGSAK Fremmednøkkel for kobling til fagsak';

comment on column HISTORIKKINNSLAG.BRUKER_KJOENN is 'FK:BRUKER_KJOENN Fremmednøkkel til kodeverkstabellen som viser mulige kjønn for bruker og viser til brukerens kjønn';

create index IDX_HISTORIKKINNSLAG_1
    on HISTORIKKINNSLAG (BEHANDLING_ID);

create index IDX_HISTORIKKINNSLAG_2
    on HISTORIKKINNSLAG (BRUKER_KJOENN);

create index IDX_HISTORIKKINNSLAG_3
    on HISTORIKKINNSLAG (HISTORIKKINNSLAG_TYPE);

create index IDX_HISTORIKKINNSLAG_4
    on HISTORIKKINNSLAG (FAGSAK_ID);

create index IDX_HISTORIKKINNSLAG_5
    on HISTORIKKINNSLAG (HISTORIKK_AKTOER_ID);

create table HISTORIKKINNSLAG_DEL
(
    ID                  NUMBER(19)                             not null
        constraint PK_HISTORIKKINNSLAG_DEL
        primary key,
    HISTORIKKINNSLAG_ID NUMBER(19)                             not null
        constraint FK_HISTORIKKINNSLAG_DEL_1
        references HISTORIKKINNSLAG,
    VERSJON             NUMBER(19)        default 0            not null,
    OPPRETTET_AV        VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID       TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV           VARCHAR2(20 char),
    ENDRET_TID          TIMESTAMP(3)
);

comment on table HISTORIKKINNSLAG_DEL is 'Et historikkinnslag kan ha en eller flere deler';

comment on column HISTORIKKINNSLAG_DEL.ID is 'Primærnøkkel';

comment on column HISTORIKKINNSLAG_DEL.HISTORIKKINNSLAG_ID is 'FK:HISTORIKKINNSLAG Fremmednøkkel til riktig innslag i historikktabellen';

create index IDX_HISTORIKKINNSLAG_DEL_1
    on HISTORIKKINNSLAG_DEL (HISTORIKKINNSLAG_ID);

create table HISTORIKKINNSLAG_DOK_LINK
(
    ID                  NUMBER(19)                             not null
        constraint PK_HISTORIKKINNSLAG_DOK_LINK
        primary key,
    LINK_TEKST          VARCHAR2(100 char)                     not null,
    HISTORIKKINNSLAG_ID NUMBER(19)                             not null
        constraint FK_HISTORIKKINNSLAG_DOK_LINK_1
        references HISTORIKKINNSLAG,
    JOURNALPOST_ID      VARCHAR2(100 char),
    DOKUMENT_ID         VARCHAR2(100 char),
    OPPRETTET_AV        VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID       TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV           VARCHAR2(20 char),
    ENDRET_TID          TIMESTAMP(3)
);

comment on table HISTORIKKINNSLAG_DOK_LINK is 'Kobling fra historikkinnslag til aktuell dokumentasjon';

comment on column HISTORIKKINNSLAG_DOK_LINK.ID is 'Primary Key';

comment on column HISTORIKKINNSLAG_DOK_LINK.LINK_TEKST is 'Tekst som vises for link til dokumentet';

comment on column HISTORIKKINNSLAG_DOK_LINK.HISTORIKKINNSLAG_ID is 'FK:HISTORIKKINNSLAG Fremmednøkkel til riktig innslag i historikktabellen';

comment on column HISTORIKKINNSLAG_DOK_LINK.JOURNALPOST_ID is 'Journal post Id';

comment on column HISTORIKKINNSLAG_DOK_LINK.DOKUMENT_ID is 'Dokumnent Id';

create index IDX_HISTINNSLAG_DOK_LINK_1
    on HISTORIKKINNSLAG_DOK_LINK (HISTORIKKINNSLAG_ID);

create table HISTORIKKINNSLAG_FELT
(
    ID                         NUMBER(19)                             not null
        constraint PK_HISTORIKKINNSLAG_FELT
        primary key,
    HISTORIKKINNSLAG_DEL_ID    NUMBER(19)                             not null
        constraint FK_HISTORIKKINNSLAG_FELT_1
        references HISTORIKKINNSLAG_DEL,
    HISTORIKKINNSLAG_FELT_TYPE VARCHAR2(100 char)                     not null,
    NAVN                       VARCHAR2(100 char),
    KL_NAVN                    VARCHAR2(100 char),
    NAVN_VERDI                 VARCHAR2(4000 char),
    FRA_VERDI                  VARCHAR2(4000 char),
    TIL_VERDI                  VARCHAR2(4000 char),
    SEKVENS_NR                 NUMBER(5),
    VERSJON                    NUMBER(19)        default 0            not null,
    OPPRETTET_AV               VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID              TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV                  VARCHAR2(20 char),
    ENDRET_TID                 TIMESTAMP(3),
    FRA_VERDI_KODE             VARCHAR2(100 char),
    TIL_VERDI_KODE             VARCHAR2(100 char),
    KL_FRA_VERDI               VARCHAR2(100 char),
    KL_TIL_VERDI               VARCHAR2(100 char)
);

comment on table HISTORIKKINNSLAG_FELT is 'En historikkinnslagdel har typisk mange felt';

comment on column HISTORIKKINNSLAG_FELT.ID is 'Primærnøkkel';

comment on column HISTORIKKINNSLAG_FELT.HISTORIKKINNSLAG_DEL_ID is 'FK:HISTORIKKINNSLAG_DEL';

comment on column HISTORIKKINNSLAG_FELT.HISTORIKKINNSLAG_FELT_TYPE is 'Hva slags type informasjon som er representert';

comment on column HISTORIKKINNSLAG_FELT.NAVN is 'Navn på felt. Gjelder for endrede verdier og opplysninger';

comment on column HISTORIKKINNSLAG_FELT.KL_NAVN is 'Hvilket kodeverk/enum som er brukt for NAVN';

comment on column HISTORIKKINNSLAG_FELT.NAVN_VERDI is 'Verdi som skal brukes som del av feltets navn';

comment on column HISTORIKKINNSLAG_FELT.FRA_VERDI is 'Feltets gamle verdi. Kun string';

comment on column HISTORIKKINNSLAG_FELT.TIL_VERDI is 'Feltets nye verdi. Kun string';

comment on column HISTORIKKINNSLAG_FELT.SEKVENS_NR is 'Settes dersom historikkinnslagdelen har flere innslag med samme navn';

comment on column HISTORIKKINNSLAG_FELT.FRA_VERDI_KODE is 'Feltets gamle verdi. Kun kodeverk';

comment on column HISTORIKKINNSLAG_FELT.TIL_VERDI_KODE is 'Feltets nye verdi. Kun kodeverk';

comment on column HISTORIKKINNSLAG_FELT.KL_FRA_VERDI is 'Hvilket kodeverk/enum som er brukt for FRA_VERDI';

comment on column HISTORIKKINNSLAG_FELT.KL_TIL_VERDI is 'Hvilket kodeverk/enum som er brukt for TIL_VERDI';

create index IDX_HISTORIKKINNSLAG_FELT_1
    on HISTORIKKINNSLAG_FELT (HISTORIKKINNSLAG_DEL_ID);

create index IDX_HISTORIKKINNSLAG_FELT_2
    on HISTORIKKINNSLAG_FELT (NAVN, KL_NAVN);

create index IDX_HISTORIKKINNSLAG_FELT_3
    on HISTORIKKINNSLAG_FELT (TIL_VERDI_KODE, KL_TIL_VERDI);

create index IDX_HISTORIKKINNSLAG_FELT_4
    on HISTORIKKINNSLAG_FELT (FRA_VERDI_KODE, KL_FRA_VERDI);

create table KRAV_GRUNNLAG_431
(
    ID                      NUMBER(19)                             not null
        constraint PK_KRAV_GRUNNLAG_431
        primary key,
    VEDTAK_ID               NUMBER(19)                             not null,
    KRAV_STATUS_KODE        VARCHAR2(100 char)                     not null,
    FAG_OMRAADE_KODE        VARCHAR2(100 char)                     not null,
    FAGSYSTEM_ID            VARCHAR2(30 char)                      not null,
    VEDTAK_FAGSYSTEM_DATO   DATE,
    OMGJORT_VEDTAK_ID       NUMBER(19),
    GJELDER_VEDTAK_ID       VARCHAR2(50 char)                      not null,
    GJELDER_TYPE            VARCHAR2(100 char)                     not null,
    UTBETALES_TIL_ID        VARCHAR2(50 char)                      not null,
    UTBET_ID_TYPE           VARCHAR2(100 char)                     not null,
    HJEMMEL_KODE            VARCHAR2(20 char),
    BEREGNES_RENTER         VARCHAR2(1 char),
    ANSVARLIG_ENHET         VARCHAR2(13 char)                      not null,
    BOSTED_ENHET            VARCHAR2(13 char)                      not null,
    BEHANDL_ENHET           VARCHAR2(13 char)                      not null,
    KONTROLL_FELT           VARCHAR2(26 char)                      not null,
    SAKSBEH_ID              VARCHAR2(20 char)                      not null,
    REFERANSE               VARCHAR2(30 char),
    OPPRETTET_AV            VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID           TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3),
    EKSTERN_KRAVGRUNNLAG_ID VARCHAR2(9 char)
);

comment on table KRAV_GRUNNLAG_431 is 'Tabell for tilbakekrevingsvedtak fra økonomi';

comment on column KRAV_GRUNNLAG_431.VEDTAK_ID is 'Identifikasjon av tilbakekrevingsvedtaket opprettet av Tilbakekrevingskomponenten';

comment on column KRAV_GRUNNLAG_431.KRAV_STATUS_KODE is 'Status på kravgrunnlaget';

comment on column KRAV_GRUNNLAG_431.FAG_OMRAADE_KODE is 'Fagområdet på feilutbetalingen';

comment on column KRAV_GRUNNLAG_431.FAGSYSTEM_ID is 'Fagsystemets identifikasjon av vedtaket som har feilutbetaling';

comment on column KRAV_GRUNNLAG_431.VEDTAK_FAGSYSTEM_DATO is 'Fagsystemets vedtaksdato for vedtaket';

comment on column KRAV_GRUNNLAG_431.OMGJORT_VEDTAK_ID is 'Henvisning til forrige gyldige vedtak';

comment on column KRAV_GRUNNLAG_431.GJELDER_VEDTAK_ID is 'Vanligvis stønadsmottaker (fnr/orgnr) i feilutbetalingen';

comment on column KRAV_GRUNNLAG_431.GJELDER_TYPE is 'Angir om Vedtak-gjelder-id er fnr, orgnr, TSS-nr etc';

comment on column KRAV_GRUNNLAG_431.UTBETALES_TIL_ID is 'Mottaker av pengene i feilutbetalingen';

comment on column KRAV_GRUNNLAG_431.UTBET_ID_TYPE is 'Angir om Utbetales-til-id er fnr, orgnr, TSS-nr etc';

comment on column KRAV_GRUNNLAG_431.HJEMMEL_KODE is 'Lovhjemmel for tilbakekrevingsvedtaket';

comment on column KRAV_GRUNNLAG_431.BEREGNES_RENTER is 'J dersom det skal beregnes renter på kravet';

comment on column KRAV_GRUNNLAG_431.ANSVARLIG_ENHET is 'Enhet ansvarlig';

comment on column KRAV_GRUNNLAG_431.BOSTED_ENHET is 'Bostedsenhet, hentet fra feilutbetalingen';

comment on column KRAV_GRUNNLAG_431.BEHANDL_ENHET is 'Behandlende enhet, hentet fra feilutbetalingen';

comment on column KRAV_GRUNNLAG_431.KONTROLL_FELT is 'Brukes ved innsending av tilbakekrevingsvedtak for å kontrollere at kravgrunnlaget ikke er blitt endret i mellomtiden';

comment on column KRAV_GRUNNLAG_431.SAKSBEH_ID is 'Saksbehandler';

comment on column KRAV_GRUNNLAG_431.REFERANSE is 'Henvisning fra nyeste oppdragslinje';

comment on column KRAV_GRUNNLAG_431.EKSTERN_KRAVGRUNNLAG_ID is 'Referanse til kravgrunnlagID fra OSTBK. Brukes ved omgjøring for å hente nytt grunnlag.';

create index IDX_KRAV_GRUNNLAG_431_1
    on KRAV_GRUNNLAG_431 (KRAV_STATUS_KODE);

create index IDX_KRAV_GRUNNLAG_431_2
    on KRAV_GRUNNLAG_431 (FAG_OMRAADE_KODE);

create index IDX_KRAV_GRUNNLAG_431_3
    on KRAV_GRUNNLAG_431 (GJELDER_TYPE);

create index IDX_KRAV_GRUNNLAG_431_4
    on KRAV_GRUNNLAG_431 (UTBET_ID_TYPE);

create index IDX_KRAV_GRUNNLAG_431_5
    on KRAV_GRUNNLAG_431 (UTBETALES_TIL_ID);

create index IDX_KRAV_GRUNNLAG_431_6
    on KRAV_GRUNNLAG_431 (VEDTAK_ID);

create table KRAV_GRUNNLAG_PERIODE_432
(
    ID                   NUMBER(19)                             not null
        constraint PK_KRAV_GRUNNLAG_PERIODE_432
        primary key,
    FOM                  DATE                                   not null,
    TOM                  DATE                                   not null,
    KRAV_GRUNNLAG_431_ID NUMBER(19)                             not null
        constraint FK_KRAV_GRUNNLAG_PERIODE_432_1
        references KRAV_GRUNNLAG_431,
    OPPRETTET_AV         VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID        TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV            VARCHAR2(20 char),
    ENDRET_TID           TIMESTAMP(3),
    BELOP_SKATT_MND      NUMBER(12, 2)                          not null
);

comment on table KRAV_GRUNNLAG_PERIODE_432 is 'Perioder av tilbakekrevingsvedtak fra økonomi';

comment on column KRAV_GRUNNLAG_PERIODE_432.FOM is 'Første dag i periode';

comment on column KRAV_GRUNNLAG_PERIODE_432.TOM is 'Siste dag i periode';

comment on column KRAV_GRUNNLAG_PERIODE_432.KRAV_GRUNNLAG_431_ID is 'FK:KRAV_GRUNNLAG_431';

comment on column KRAV_GRUNNLAG_PERIODE_432.BELOP_SKATT_MND is 'Angir total skatt beløp per måned';

create index IDX_KRAV_GRUNN_PERIODE_432_1
    on KRAV_GRUNNLAG_PERIODE_432 (KRAV_GRUNNLAG_431_ID);

create table KRAV_GRUNNLAG_BELOP_433
(
    ID                           NUMBER(19)                             not null
        constraint PK_KRAV_GRUNNLAG_BELOP_433
        primary key,
    KLASSE_KODE                  VARCHAR2(100 char)                     not null,
    KLASSE_TYPE                  VARCHAR2(100 char)                     not null,
    OPPR_UTBET_BELOP             NUMBER(12, 2),
    NY_BELOP                     NUMBER(12, 2)                          not null,
    TILBAKE_KREVES_BELOP         NUMBER(12, 2),
    UINNKREVD_BELOP              NUMBER(12, 2),
    RESULTAT_KODE                VARCHAR2(20 char),
    AARSAK_KODE                  VARCHAR2(20 char),
    SKYLD_KODE                   VARCHAR2(20 char),
    KRAV_GRUNNLAG_PERIODE_432_ID NUMBER(19)                             not null
        constraint FK_KRAV_GRUNNLAG_BELOP_433_3
        references KRAV_GRUNNLAG_PERIODE_432,
    OPPRETTET_AV                 VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID                TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV                    VARCHAR2(20 char),
    ENDRET_TID                   TIMESTAMP(3),
    SKATT_PROSENT                NUMBER(7, 4)                           not null
);

comment on table KRAV_GRUNNLAG_BELOP_433 is 'Tabell for tilbakekrevingsbeløp fra økonomi';

comment on column KRAV_GRUNNLAG_BELOP_433.KLASSE_KODE is 'Klassifisering av stønad, skatt, trekk etc.';

comment on column KRAV_GRUNNLAG_BELOP_433.KLASSE_TYPE is 'Angir type av klassekoden';

comment on column KRAV_GRUNNLAG_BELOP_433.OPPR_UTBET_BELOP is 'Opprinnelig beregnet beløp, dvs utbetalingen som førte til feilutbetaling';

comment on column KRAV_GRUNNLAG_BELOP_433.NY_BELOP is 'Beløpet som ble beregnet ved korrigeringen';

comment on column KRAV_GRUNNLAG_BELOP_433.TILBAKE_KREVES_BELOP is 'Beløpet som skal tilbakekreves';

comment on column KRAV_GRUNNLAG_BELOP_433.UINNKREVD_BELOP is 'Beløp som ikke skal tilbakekreves';

comment on column KRAV_GRUNNLAG_BELOP_433.RESULTAT_KODE is 'Hvilket vedtak som er fattet ang tilbakekreving';

comment on column KRAV_GRUNNLAG_BELOP_433.AARSAK_KODE is 'Årsak til feilutbetalingen';

comment on column KRAV_GRUNNLAG_BELOP_433.SKYLD_KODE is 'Hvem som har skyld i at det ble feilutbetalt';

comment on column KRAV_GRUNNLAG_BELOP_433.KRAV_GRUNNLAG_PERIODE_432_ID is 'FK:KRAV_GRUNNLAG_PERIODE_432';

comment on column KRAV_GRUNNLAG_BELOP_433.SKATT_PROSENT is 'Angir gjeldende skatt prosent som skal trekke fra brutto tilbakekrevingsbeløp for nettotilbakekreving';

create index IDX_KRAV_GRUNN_BELOP_433_1
    on KRAV_GRUNNLAG_BELOP_433 (KLASSE_KODE);

create index IDX_KRAV_GRUNN_BELOP_433_2
    on KRAV_GRUNNLAG_BELOP_433 (KLASSE_TYPE);

create index IDX_KRAV_GRUNN_BELOP_433_3
    on KRAV_GRUNNLAG_BELOP_433 (KRAV_GRUNNLAG_PERIODE_432_ID);

create table KRAV_VEDTAK_STATUS_437
(
    ID                NUMBER(19)                             not null
        constraint PK_KRAV_VEDTAK_STATUS_437
        primary key,
    VEDTAK_ID         NUMBER(19)                             not null,
    KRAV_STATUS_KODE  VARCHAR2(100 char)                     not null,
    FAG_OMRAADE_KODE  VARCHAR2(100 char)                     not null,
    FAGSYSTEM_ID      VARCHAR2(30 char)                      not null,
    GJELDER_VEDTAK_ID VARCHAR2(50 char)                      not null,
    GJELDER_TYPE      VARCHAR2(100 char)                     not null,
    REFERANSE         VARCHAR2(30 char),
    OPPRETTET_AV      VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID     TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV         VARCHAR2(20 char),
    ENDRET_TID        TIMESTAMP(3)
);

comment on table KRAV_VEDTAK_STATUS_437 is 'Tabell for krav og vedtak status endringer fra økonomi';

comment on column KRAV_VEDTAK_STATUS_437.VEDTAK_ID is 'Identifikasjon av tilbakekrevingsvedtaket opprettet av Tilbakekrevingskomponenten';

comment on column KRAV_VEDTAK_STATUS_437.KRAV_STATUS_KODE is 'Status på kravgrunnlaget';

comment on column KRAV_VEDTAK_STATUS_437.FAG_OMRAADE_KODE is 'Fagområdet på feilutbetalingen';

comment on column KRAV_VEDTAK_STATUS_437.FAGSYSTEM_ID is 'Fagsystemets identifikasjon av vedtaket som har feilutbetaling';

comment on column KRAV_VEDTAK_STATUS_437.GJELDER_VEDTAK_ID is 'Vanligvis stønadsmottaker (fnr/orgnr) i feilutbetalingen';

comment on column KRAV_VEDTAK_STATUS_437.GJELDER_TYPE is 'Angir om Vedtak-gjelder-id er fnr, orgnr, TSS-nr etc';

comment on column KRAV_VEDTAK_STATUS_437.REFERANSE is 'Henvisning fra nyeste oppdragslinje';

create index IDX_KRAV_VED_STATUS_437_1
    on KRAV_VEDTAK_STATUS_437 (KRAV_STATUS_KODE);

create index IDX_KRAV_VED_STATUS_437_2
    on KRAV_VEDTAK_STATUS_437 (FAG_OMRAADE_KODE);

create index IDX_KRAV_VED_STATUS_437_3
    on KRAV_VEDTAK_STATUS_437 (GJELDER_TYPE);

create table GR_KRAV_GRUNNLAG
(
    ID                  NUMBER(19)                              not null
        constraint PK_GR_KRAV_GRUNNLAG
        primary key,
    GRUNNLAG_OKONOMI_ID NUMBER(19)                              not null
        constraint FK_GR_KRAV_GRUNNLAG_2
        references KRAV_GRUNNLAG_431,
    BEHANDLING_ID       NUMBER(19)                              not null
        constraint FK_GR_KRAV_GRUNNLAG_1
        references BEHANDLING,
    AKTIV               VARCHAR2(1)        default 'J'          not null,
    OPPRETTET_AV        VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID       TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV           VARCHAR2(20 char),
    ENDRET_TID          TIMESTAMP(3),
    SPERRET             VARCHAR2(1),
    VERSJON             NUMBER(19)         default 0            not null
);

comment on table GR_KRAV_GRUNNLAG is 'Aggregate tabell for å lagre grunnlag';

comment on column GR_KRAV_GRUNNLAG.ID is 'Primary Key';

comment on column GR_KRAV_GRUNNLAG.GRUNNLAG_OKONOMI_ID is 'FK:KRAV_GRUNNLAG_431.Angir grunnlag kommer fra okonomi';

comment on column GR_KRAV_GRUNNLAG.BEHANDLING_ID is 'FK: BEHANDLING fremmednøkkel for tilknyttet behandling';

comment on column GR_KRAV_GRUNNLAG.AKTIV is 'Angir status av grunnlag';

comment on column GR_KRAV_GRUNNLAG.SPERRET is 'Angir om grunnlaget har fått sper melding fra økonomi';

create index IDX_GR_KRAV_GRUNNLAG_1
    on GR_KRAV_GRUNNLAG (GRUNNLAG_OKONOMI_ID);

create index IDX_GR_KRAV_GRUNNLAG_2
    on GR_KRAV_GRUNNLAG (BEHANDLING_ID);

create table VILKAAR
(
    ID            NUMBER(19)                             not null
        constraint PK_VILKAAR
        primary key,
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3)
);

comment on table VILKAAR is 'Kobler flere perioder av vilkårsvurdering for tilbakekreving';

create table GR_VILKAAR
(
    ID                 NUMBER(19)                             not null
        constraint PK_GR_VILKAAR
        primary key,
    BEHANDLING_ID      NUMBER(19)                             not null,
    MANUELL_VILKAAR_ID NUMBER(19)                             not null
        constraint FK_GR_VILKAAR_1
        references VILKAAR,
    AKTIV              VARCHAR2(1 char)                       not null
        constraint CHK_AKTIV
        check (AKTIV in ('J', 'N')),
    OPPRETTET_AV       VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID      TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV          VARCHAR2(20 char),
    ENDRET_TID         TIMESTAMP(3),
    VERSJON            NUMBER(19)        default 0            not null
);

comment on table GR_VILKAAR is 'Versjonering av vilkårsvurdering for tilbakekreving';

comment on column GR_VILKAAR.BEHANDLING_ID is 'Referanse til behandling';

comment on column GR_VILKAAR.MANUELL_VILKAAR_ID is 'Peker på saksbehandlers valg for manuell vilkårvurdering';

comment on column GR_VILKAAR.AKTIV is 'Angir status av manuell vilkårvurdering';

create index IDX_GR_VILKAAR_1
    on GR_VILKAAR (BEHANDLING_ID);

create index IDX_GR_VILKAAR_2
    on GR_VILKAAR (MANUELL_VILKAAR_ID);

create table VILKAAR_PERIODE
(
    ID               NUMBER(19)                             not null
        constraint PK_VILKAAR_PERIODE
        primary key,
    VILKAAR_ID       NUMBER(19)                             not null
        constraint FK_VILKAAR_PERIODE_1
        references VILKAAR,
    FOM              DATE                                   not null,
    TOM              DATE                                   not null,
    NAV_OPPFULGT     VARCHAR2(100 char)                     not null,
    VILKAAR_RESULTAT VARCHAR2(100 char)                     not null,
    BEGRUNNELSE      VARCHAR2(4000 char)                    not null,
    OPPRETTET_AV     VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID    TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV        VARCHAR2(20 char),
    ENDRET_TID       TIMESTAMP(3)
);

comment on table VILKAAR_PERIODE is 'Periode med vilkårsvurdering for tilbakekreving';

comment on column VILKAAR_PERIODE.VILKAAR_ID is 'FK:VILKAAR';

comment on column VILKAAR_PERIODE.FOM is 'Fra-og-med-dato';

comment on column VILKAAR_PERIODE.TOM is 'Til-og-med-dato';

comment on column VILKAAR_PERIODE.NAV_OPPFULGT is 'Vurdering av hvordan nav har fulgt opp';

comment on column VILKAAR_PERIODE.VILKAAR_RESULTAT is 'Hovedresultat av vilkårsvurdering (kodeverk)';

comment on column VILKAAR_PERIODE.BEGRUNNELSE is 'Saksbehandlers begrunnelse';

create index IDX_VILKAAR_PERIODE_1
    on VILKAAR_PERIODE (VILKAAR_ID);

create index IDX_VILKAAR_PERIODE_2
    on VILKAAR_PERIODE (NAV_OPPFULGT);

create index IDX_VILKAAR_PERIODE_3
    on VILKAAR_PERIODE (VILKAAR_RESULTAT);

create table VILKAAR_AKTSOMHET
(
    ID                           NUMBER(19)                             not null
        constraint PK_VILKAAR_AKTSOMHET
        primary key,
    VILKAAR_PERIODE_ID           NUMBER(19)                             not null
        constraint FK_VILKAAR_AKTSOMHET_1
        references VILKAAR_PERIODE,
    AKTSOMHET                    VARCHAR2(100 char)                     not null,
    ILEGG_RENTER                 VARCHAR2(1 char)
        constraint CHK_RENTER
        check (ILEGG_RENTER is null or ILEGG_RENTER in ('J', 'N')),
    ANDEL_TILBAKEKREVES          NUMBER(5, 2),
    MANUELT_SATT_BELOEP          NUMBER(19),
    BEGRUNNELSE                  VARCHAR2(4000 char)                    not null,
    OPPRETTET_AV                 VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID                TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV                    VARCHAR2(20 char),
    ENDRET_TID                   TIMESTAMP(3),
    SARLIG_GRUNNER_TIL_REDUKSJON VARCHAR2(1 char)
        constraint CHK_GRUNNER_TIL_REDUSKSJON
        check (SARLIG_GRUNNER_TIL_REDUKSJON is null or SARLIG_GRUNNER_TIL_REDUKSJON in ('J', 'N')),
    TILBAKEKREV_SMAABELOEP       VARCHAR2(1 char)
        constraint CHK_TILBAKEKREV_SMAABELOEP
        check (TILBAKEKREV_SMAABELOEP is null or TILBAKEKREV_SMAABELOEP in ('J', 'N')),
    SARLIG_GRUNNER_BEGRUNNELSE   VARCHAR2(4000 char),
    constraint CHK_ANDEL_BELOP
        check ("ANDEL_TILBAKEKREVES" IS NULL OR "MANUELT_SATT_BELOEP" IS NULL)
);

comment on table VILKAAR_AKTSOMHET is 'Videre vurderinger når det er vurdert at bruker ikke mottok beløp i god tro';

comment on column VILKAAR_AKTSOMHET.VILKAAR_PERIODE_ID is 'FK:VILKAAR_PERIODE';

comment on column VILKAAR_AKTSOMHET.AKTSOMHET is 'Resultat av aktsomhetsvurdering (kodeverk)';

comment on column VILKAAR_AKTSOMHET.ILEGG_RENTER is 'Hvorvidt renter skal ilegges';

comment on column VILKAAR_AKTSOMHET.ANDEL_TILBAKEKREVES is 'Hvor stor del av feilutbetalt beløp som skal tilbakekreves';

comment on column VILKAAR_AKTSOMHET.MANUELT_SATT_BELOEP is 'Feilutbetalt beløp som skal tilbakekreves som bestemt ved saksbehandler';

comment on column VILKAAR_AKTSOMHET.BEGRUNNELSE is 'beskrivelse av aktsomhet';

comment on column VILKAAR_AKTSOMHET.SARLIG_GRUNNER_TIL_REDUKSJON is 'Angir om særlig grunner gi reduksjon av beløpet';

comment on column VILKAAR_AKTSOMHET.TILBAKEKREV_SMAABELOEP is 'Angir om skal tilbakekreves når totalbeløpet er under 4 rettsgebyr';

comment on column VILKAAR_AKTSOMHET.SARLIG_GRUNNER_BEGRUNNELSE is 'beskrivelse av særlig grunner';

create index IDX_VILKAAR_AKTSOMHET_1
    on VILKAAR_AKTSOMHET (VILKAAR_PERIODE_ID);

create index IDX_VILKAAR_AKTSOMHET_2
    on VILKAAR_AKTSOMHET (AKTSOMHET);

create table VILKAAR_SAERLIG_GRUNN
(
    ID                  NUMBER(19)                             not null
        constraint PK_VILKAAR_SAERLIG_GRUNN
        primary key,
    VURDER_AKTSOMHET_ID NUMBER(19)                             not null
        constraint FK_VILKAAR_SAERLIG_GRUNN_1
        references VILKAAR_AKTSOMHET,
    SAERLIG_GRUNN       VARCHAR2(100 char)                     not null,
    OPPRETTET_AV        VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID       TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV           VARCHAR2(20 char),
    ENDRET_TID          TIMESTAMP(3),
    BEGRUNNELSE         VARCHAR2(4000 char)
);

comment on table VILKAAR_SAERLIG_GRUNN is 'Særlige grunner ved vurdering';

comment on column VILKAAR_SAERLIG_GRUNN.VURDER_AKTSOMHET_ID is 'FK:VILKAAR_AKTSOMHET';

comment on column VILKAAR_SAERLIG_GRUNN.SAERLIG_GRUNN is 'Særlig grunn (kodeverk)';

comment on column VILKAAR_SAERLIG_GRUNN.BEGRUNNELSE is 'Beskrivelse av særlig grunn hvis grunn er annet';

create index IDX_VILKAAR_SAERLIG_GRUNN_1
    on VILKAAR_SAERLIG_GRUNN (VURDER_AKTSOMHET_ID);

create index IDX_VILKAAR_SAERLIG_GRUNN_2
    on VILKAAR_SAERLIG_GRUNN (SAERLIG_GRUNN);

create table VILKAAR_GOD_TRO
(
    ID                   NUMBER(19)                             not null
        constraint PK_VILKAAR_GOD_TRO
        primary key,
    VILKAAR_PERIODE_ID   NUMBER(19)                             not null
        constraint FK_VILKAAR_GOD_TRO_1
        references VILKAAR_PERIODE,
    BELOEP_ER_I_BEHOLD   VARCHAR2(1 char)                       not null,
    BELOEP_TILBAKEKREVES NUMBER(19),
    BEGRUNNELSE          VARCHAR2(4000 char)                    not null,
    OPPRETTET_AV         VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID        TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV            VARCHAR2(20 char),
    ENDRET_TID           TIMESTAMP(3)
);

comment on table VILKAAR_GOD_TRO is 'Videre vurderinger når det er vurdert at bruker mottok feilutbetaling i god tro';

comment on column VILKAAR_GOD_TRO.VILKAAR_PERIODE_ID is 'FK:VILKAAR_PERIODE';

comment on column VILKAAR_GOD_TRO.BELOEP_ER_I_BEHOLD is 'Indikerer at beløp er i behold';

comment on column VILKAAR_GOD_TRO.BELOEP_TILBAKEKREVES is 'Hvor mye av feilutbetalt beløp som skal tilbakekreves';

comment on column VILKAAR_GOD_TRO.BEGRUNNELSE is 'beskrivelse av god tro vilkaar';

create index IDX_VILKAAR_GOD_TRO
    on VILKAAR_GOD_TRO (VILKAAR_PERIODE_ID);

create table EKSTERN_BEHANDLING
(
    ID            NUMBER(19)                        not null
        constraint PK_EKSTERN_BEHANDLING
        primary key,
    INTERN_ID     NUMBER(19)                        not null
        constraint FK_EKSTERN_BEHANDLING_1
        references BEHANDLING,
    AKTIV         VARCHAR2(1 char)  default 'J'     not null,
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'    not null,
    OPPRETTET_TID TIMESTAMP(3)      default sysdate not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    EKSTERN_UUID  RAW(16),
    HENVISNING    VARCHAR2(30 char)                 not null,
    VERSJON       NUMBER(19)        default 0       not null,
    constraint UIDX_EKSTERN_BEHANDLING_2
        unique (INTERN_ID, HENVISNING)
);

comment on table EKSTERN_BEHANDLING is 'Referanse til ekstern behandling';

comment on column EKSTERN_BEHANDLING.ID is 'Primary key';

comment on column EKSTERN_BEHANDLING.INTERN_ID is 'FK: Behandling Fremmednøkkel for kobling til intern behandling';

comment on column EKSTERN_BEHANDLING.AKTIV is 'Angir om ekstern behandling data er gjeldende';

comment on column EKSTERN_BEHANDLING.EKSTERN_UUID is 'Unik UUID for ekstern-behandling';

comment on column EKSTERN_BEHANDLING.HENVISNING is 'Henvisning/referanse. Peker på referanse-feltet i kravgrunnlaget, og kommer opprinnelig fra fagsystemet. For fptilbake er den lik fpsak.behandlingId. For k9-tilbake er den lik base64(bytes(behandlingUuid))';

create index IDX_EKSTERN_BEHANDLING_1
    on EKSTERN_BEHANDLING (INTERN_ID);

create index IDX_EKSTERN_BEHANDLING_3
    on EKSTERN_BEHANDLING (EKSTERN_UUID);

create index IDX_EKSTERN_BEHANDLING_4
    on EKSTERN_BEHANDLING (HENVISNING);

create table FAKTA_FEILUTBETALING
(
    ID            NUMBER(19)                             not null
        constraint PK_FAKTA_FEILUTBETALING
        primary key,
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)      default SYSTIMESTAMP not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    BEGRUNNELSE   VARCHAR2(4000 char)
);

comment on table FAKTA_FEILUTBETALING is 'Kobler flere perioder av fakta om feilutbetaling for tilbakekreving';

comment on column FAKTA_FEILUTBETALING.BEGRUNNELSE is 'Begrunnelse for endringer gjort i fakta om feilutbetaling';

create table FAKTA_FEILUTBETALING_PERIODE
(
    ID                      NUMBER(19)                             not null
        constraint PK_FAKTA_FEILUTB_PERIODE
        primary key,
    FOM                     DATE                                   not null,
    TOM                     DATE                                   not null,
    HENDELSE_TYPE           VARCHAR2(100 char)                     not null,
    HENDELSE_UNDERTYPE      VARCHAR2(100 char)                     not null,
    OPPRETTET_AV            VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID           TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3),
    FAKTA_FEILUTBETALING_ID NUMBER(19)                             not null
        constraint FK_FAKTA_FEILUTB_PERIODE_1
        references FAKTA_FEILUTBETALING
);

comment on table FAKTA_FEILUTBETALING_PERIODE is 'Tabell for å lagere feilutbetaling årsak og underårsak for hver perioder';

comment on column FAKTA_FEILUTBETALING_PERIODE.ID is 'Primary Key';

comment on column FAKTA_FEILUTBETALING_PERIODE.FOM is 'Første dag av feilutbetaling periode';

comment on column FAKTA_FEILUTBETALING_PERIODE.TOM is 'Siste dag av feilutbetaling periode';

comment on column FAKTA_FEILUTBETALING_PERIODE.HENDELSE_TYPE is 'Hendelse som er årsak til feilutbetalingen';

comment on column FAKTA_FEILUTBETALING_PERIODE.HENDELSE_UNDERTYPE is 'Hendelse som er årsak til feilutbetalingen (underårsak)';

comment on column FAKTA_FEILUTBETALING_PERIODE.FAKTA_FEILUTBETALING_ID is 'FK:FEILUTBETALING';

create index IDX_FEILUTBET_PERIODE_AARSAK_2
    on FAKTA_FEILUTBETALING_PERIODE (FAKTA_FEILUTBETALING_ID);

create index IDX_FAKTA_FEILUT_PERIODE_1
    on FAKTA_FEILUTBETALING_PERIODE (HENDELSE_TYPE);

create index IDX_FAKTA_FEILUT_PERIODE_2
    on FAKTA_FEILUTBETALING_PERIODE (HENDELSE_UNDERTYPE);

create table GR_FAKTA_FEILUTBETALING
(
    ID                      NUMBER(19)                             not null
        constraint PK_GR_FAKTA_FEILUTBETALING
        primary key,
    BEHANDLING_ID           NUMBER(19)                             not null
        constraint FK_GR_FAKTA_FEILUTB_1
        references BEHANDLING,
    FAKTA_FEILUTBETALING_ID NUMBER(19)                             not null
        constraint FK_GR_FAKTA_FEILUTB_2
        references FAKTA_FEILUTBETALING,
    AKTIV                   VARCHAR2(1 char)                       not null
        constraint CHK_GR_FAKTA_FEILUTB_AKTIV
        check (AKTIV IN ('J', 'N')),
    OPPRETTET_AV            VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID           TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3),
    VERSJON                 NUMBER(19)        default 0            not null
);

comment on table GR_FAKTA_FEILUTBETALING is 'Versjonering av fakta om feilutbetaling for tilbakekreving';

comment on column GR_FAKTA_FEILUTBETALING.BEHANDLING_ID is 'Referanse til behandling';

comment on column GR_FAKTA_FEILUTBETALING.FAKTA_FEILUTBETALING_ID is 'FK:FEILUTBETALING';

comment on column GR_FAKTA_FEILUTBETALING.AKTIV is 'Angir status av fakta om feilutbetaling';

create index IDX_GR_FAKTA_FEILUTB_1
    on GR_FAKTA_FEILUTBETALING (BEHANDLING_ID);

create index IDX_GR_FAKTA_FEILUTB_2
    on GR_FAKTA_FEILUTBETALING (FAKTA_FEILUTBETALING_ID);

create table OKO_XML_MOTTATT
(
    ID            NUMBER(19)                             not null
        constraint PK_KRAVGRUNNLAG
        primary key,
    MELDING       CLOB                                   not null,
    SEKVENS       NUMBER(19),
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    TILKOBLET     VARCHAR2(1 char),
    SAKSNUMMER    VARCHAR2(50 char),
    HENVISNING    VARCHAR2(30 char),
    VERSJON       NUMBER(19)        default 0            not null
);

comment on table OKO_XML_MOTTATT is 'Lagrer mottat kravgrunnlag-xml i påvente av at den skal prosesseres. Brukes for at mottak skal være mer robust';

comment on column OKO_XML_MOTTATT.ID is 'Primærnøkkel';

comment on column OKO_XML_MOTTATT.MELDING is 'Kravgrunnlag-xml';

comment on column OKO_XML_MOTTATT.SEKVENS is 'Teller innenfor en behandling';

comment on column OKO_XML_MOTTATT.TILKOBLET is 'Angir om mottatt xml er tilkoblet med en behandling';

comment on column OKO_XML_MOTTATT.SAKSNUMMER is 'saksnummer(som økonomi har sendt)';

comment on column OKO_XML_MOTTATT.HENVISNING is 'Henvisning/referanse. Peker på referanse-feltet i kravgrunnlaget, og kommer opprinnelig fra fagsystemet. For fptilbake er den lik fpsak.behandlingId. For k9-tilbake er den lik base64(bytes(behandlingUuid))';

create index IDX_OKO_XML_MOTTATT_2
    on OKO_XML_MOTTATT (HENVISNING);

create index IDX_OKO_XML_MOTTATT_3
    on OKO_XML_MOTTATT (SAKSNUMMER);

create index IDX_OKO_XML_MOTTATT_4
    on OKO_XML_MOTTATT (OPPRETTET_TID);

create index IDX_OKO_XML_MOTTATT_5
    on OKO_XML_MOTTATT (TILKOBLET);

create table TOTRINNRESULTATGRUNNLAG
(
    ID                      NUMBER(19)                             not null
        constraint PK_TOTRINNRESULTATGRUNNLAG
        primary key,
    BEHANDLING_ID           NUMBER(19)                             not null
        constraint FK_TOTRINNRESULTATGRUNNLAG_1
        references BEHANDLING,
    FAKTA_FEILUTBETALING_ID NUMBER(19)                             not null
        constraint FK_TOTRINNRESULTATGRUNNLAG_2
        references GR_FAKTA_FEILUTBETALING,
    VURDERT_FORELDELSE_ID   NUMBER(19)
        constraint FK_TOTRINNRESULTATGRUNNLAG_3
        references GR_VURDERT_FORELDELSE,
    VURDERT_VILKAAR_ID      NUMBER(19)
        constraint FK_TOTRINNRESULTATGRUNNLAG_4
        references GR_VILKAAR,
    AKTIV                   VARCHAR2(1 char)                       not null
        constraint CHK_TOTRINNRESGR_AKTIV
        check (AKTIV IN ('J', 'N')),
    VERSJON                 NUMBER(19)        default 0            not null,
    OPPRETTET_AV            VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID           TIMESTAMP(3)      default SYSTIMESTAMP not null,
    ENDRET_AV               VARCHAR2(20 char),
    ENDRET_TID              TIMESTAMP(3)
);

comment on table TOTRINNRESULTATGRUNNLAG is 'Tabell som held grunnlagsId for data vist i panelet fra beslutter.';

comment on column TOTRINNRESULTATGRUNNLAG.ID is 'PK';

comment on column TOTRINNRESULTATGRUNNLAG.BEHANDLING_ID is 'FK til behandling som hører til totrinn resultatet';

comment on column TOTRINNRESULTATGRUNNLAG.FAKTA_FEILUTBETALING_ID is 'FK til aktivt FeilutbetalingAggregate ved totrinnsbehandlingen';

comment on column TOTRINNRESULTATGRUNNLAG.VURDERT_FORELDELSE_ID is 'FK til aktivt VurdertForeldelseAggregate ved totrinnsbehanddlingen';

comment on column TOTRINNRESULTATGRUNNLAG.VURDERT_VILKAAR_ID is 'FK til aktivt VilkårVurderingAggregate ved totrinnsbehandlingen';

create index IDX_TORINN_RES_GR_01
    on TOTRINNRESULTATGRUNNLAG (BEHANDLING_ID);

create index IDX_TORINN_RES_GR_02
    on TOTRINNRESULTATGRUNNLAG (FAKTA_FEILUTBETALING_ID);

create index IDX_TORINN_RES_GR_03
    on TOTRINNRESULTATGRUNNLAG (VURDERT_FORELDELSE_ID);

create index IDX_TORINN_RES_GR_04
    on TOTRINNRESULTATGRUNNLAG (VURDERT_VILKAAR_ID);

create table VEDTAKSBREV_OPPSUMMERING
(
    ID                    NUMBER(19)                             not null
        constraint PK_VEDTAKSBREV_OPPSUMMERING
        primary key,
    BEHANDLING_ID         NUMBER(19)                             not null
        constraint FK_VEDTAKSBREV_OPPSUMMERING
        references BEHANDLING,
    OPPSUMMERING_FRITEKST VARCHAR2(4000 char),
    OPPRETTET_TID         TIMESTAMP(3)      default systimestamp not null,
    OPPRETTET_AV          VARCHAR2(20 char) default 'VL'         not null,
    ENDRET_AV             VARCHAR2(20 char),
    ENDRET_TID            TIMESTAMP(3),
    FRITEKST              CLOB,
    VERSJON               NUMBER(19)        default 0            not null
);

comment on table VEDTAKSBREV_OPPSUMMERING is 'Inneholder friteksten til vedtaksoppsummeringen som er skrevet inn av saksbehandler.';

comment on column VEDTAKSBREV_OPPSUMMERING.ID is 'Primary Key';

comment on column VEDTAKSBREV_OPPSUMMERING.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling i fptilbake';

comment on column VEDTAKSBREV_OPPSUMMERING.OPPSUMMERING_FRITEKST is 'Fritekst fra saksbehandler til oppsummering av vedtaket';

comment on column VEDTAKSBREV_OPPSUMMERING.FRITEKST is 'Fritekst fra saksbehandler til oppsummering av vedtaket';

create index IDX_VEDTAKSBREVOPPSUMMERING
    on VEDTAKSBREV_OPPSUMMERING (BEHANDLING_ID);

create table VEDTAKSBREV_PERIODE
(
    ID            NUMBER(19)                             not null
        constraint PK_VEDTAKSBREV_PERIODE
        primary key,
    BEHANDLING_ID NUMBER(19)                             not null
        constraint FK_VEDTAKSBREV_PERIODE
        references BEHANDLING,
    FOM           DATE                                   not null,
    TOM           DATE                                   not null,
    FRITEKST      VARCHAR2(4000 char)                    not null,
    FRITEKST_TYPE VARCHAR2(200 char)                     not null,
    OPPRETTET_TID TIMESTAMP(3)      default systimestamp not null,
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    VERSJON       NUMBER(19)        default 0            not null
);

comment on table VEDTAKSBREV_PERIODE is 'Inneholder en periode i et vedtaksbrev, samt fritekst';

comment on column VEDTAKSBREV_PERIODE.ID is 'Primary Key';

comment on column VEDTAKSBREV_PERIODE.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling i fptilbake';

comment on column VEDTAKSBREV_PERIODE.FOM is 'Fom-dato for perioden';

comment on column VEDTAKSBREV_PERIODE.TOM is 'Tom-dato for perioden';

comment on column VEDTAKSBREV_PERIODE.FRITEKST is 'Fritekst skrevet til et av avsnittene i vedtaksbrevet';

comment on column VEDTAKSBREV_PERIODE.FRITEKST_TYPE is 'Hvilket avsnitt friteksten gjelder';

create index IDX_VEDTAKSBREVPERIODER
    on VEDTAKSBREV_PERIODE (BEHANDLING_ID);

create table OKO_XML_SENDT
(
    ID            NUMBER(19)                   not null,
    BEHANDLING_ID NUMBER(19)                   not null
        constraint FK_OKO_XML_SENDT_1
        references BEHANDLING,
    MELDING       CLOB                         not null,
    KVITTERING    CLOB,
    OPPRETTET_AV  VARCHAR2(200 char) default 'VL',
    OPPRETTET_TID TIMESTAMP(3)       default SYSTIMESTAMP,
    ENDRET_AV     VARCHAR2(200 char),
    ENDRET_TID    TIMESTAMP(3),
    MELDING_TYPE  VARCHAR2(100 char)           not null,
    VERSJON       NUMBER(19)         default 0 not null
);

comment on table OKO_XML_SENDT is 'Tabell som tar vare på XML sendt til OS, brukes for feilsøking';

comment on column OKO_XML_SENDT.ID is 'Primary key';

comment on column OKO_XML_SENDT.BEHANDLING_ID is 'Behandlingen det gjelder';

comment on column OKO_XML_SENDT.MELDING is 'XML sendt til OS';

comment on column OKO_XML_SENDT.KVITTERING is 'Respons fra OS';

comment on column OKO_XML_SENDT.MELDING_TYPE is 'Melding Type';

create unique index PK_VEDTAK_XML_OS
    on OKO_XML_SENDT (ID);

create index IDX_OKO_XML_SENDT_1
    on OKO_XML_SENDT (BEHANDLING_ID);

create index IDX_OKO_XML_SENDT_2
    on OKO_XML_SENDT (MELDING_TYPE);

alter table OKO_XML_SENDT
    add constraint PK_OKO_XML_SENDT
        primary key (ID);

create table GR_KRAV_VEDTAK_STATUS
(
    ID                    NUMBER(19)                              not null
        constraint PK_GR_KRAV_VEDTAK_STATUS
        primary key,
    KRAV_VEDTAK_STATUS_ID NUMBER(19)                              not null
        constraint FK_GR_KRAV_VEDTAK_STATUS_2
        references KRAV_VEDTAK_STATUS_437,
    BEHANDLING_ID         NUMBER(19)                              not null
        constraint FK_GR_KRAV_VEDTAK_STATUS_1
        references BEHANDLING,
    AKTIV                 VARCHAR2(1)                             not null,
    OPPRETTET_AV          VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID         TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV             VARCHAR2(20 char),
    ENDRET_TID            TIMESTAMP(3),
    VERSJON               NUMBER(19)         default 0            not null
);

comment on table GR_KRAV_VEDTAK_STATUS is 'Aggregate tabell for å lagre kravOgVedtakStatus';

comment on column GR_KRAV_VEDTAK_STATUS.ID is 'Primary Key';

comment on column GR_KRAV_VEDTAK_STATUS.KRAV_VEDTAK_STATUS_ID is 'FK:KRAV_VEDTAK_STATUS_437.Angir kravOgVedtakStatus kommer fra okonomi';

comment on column GR_KRAV_VEDTAK_STATUS.BEHANDLING_ID is 'FK: BEHANDLING fremmednøkkel for tilknyttet behandling';

comment on column GR_KRAV_VEDTAK_STATUS.AKTIV is 'Angir status av grunnlag';

create index IDX_GR_KRAV_VEDTAK_STATUS_1
    on GR_KRAV_VEDTAK_STATUS (KRAV_VEDTAK_STATUS_ID);

create index IDX_GR_KRAV_VEDTAK_STATUS_2
    on GR_KRAV_VEDTAK_STATUS (BEHANDLING_ID);

create table VARSEL
(
    ID              NUMBER(19)                              not null
        constraint PK_VARSEL
        primary key,
    BEHANDLING_ID   NUMBER(19)                              not null
        constraint FK_VARSEL_1
        references BEHANDLING,
    AKTIV           VARCHAR2(1)                             not null,
    VARSEL_FRITEKST VARCHAR2(3000 char)                     not null,
    VARSEL_BELOEP   NUMBER(12),
    OPPRETTET_AV    VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID   TIMESTAMP(3)       default systimestamp not null,
    ENDRET_AV       VARCHAR2(20 char),
    ENDRET_TID      TIMESTAMP(3),
    VERSJON         NUMBER(19)         default 0            not null
);

comment on table VARSEL is 'Tabell for å lagre varsel info';

comment on column VARSEL.ID is 'Primary Key';

comment on column VARSEL.BEHANDLING_ID is 'FK: BEHANDLING fremmednøkkel for tilknyttet behandling';

comment on column VARSEL.AKTIV is 'Angir status av varsel';

comment on column VARSEL.VARSEL_FRITEKST is 'fritekst som brukes i varselbrev';

comment on column VARSEL.VARSEL_BELOEP is 'beløp som brukes i varselbrev';

create index IDX_VARSEL_1
    on VARSEL (BEHANDLING_ID);

create table BREV_SPORING
(
    ID             NUMBER(19)                             not null
        constraint PK_BREV_SPORING
        primary key,
    BEHANDLING_ID  NUMBER(19)                             not null
        constraint FK_BREV_SPORING
        references BEHANDLING,
    JOURNALPOST_ID VARCHAR2(20 char)                      not null,
    DOKUMENT_ID    VARCHAR2(20 char)                      not null,
    BREV_TYPE      VARCHAR2(100 char)                     not null,
    OPPRETTET_AV   VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID  TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV      VARCHAR2(20 char),
    ENDRET_TID     TIMESTAMP(3),
    VERSJON        NUMBER(19)        default 0            not null
);

comment on table BREV_SPORING is 'Brevsporing inneholder informasjon om forkjelige brev som er bestilt.';

comment on column BREV_SPORING.ID is 'Primary Key';

comment on column BREV_SPORING.BEHANDLING_ID is 'FK: BEHANDLING Fremmednøkkel for kobling til behandling i fptilbake';

comment on column BREV_SPORING.JOURNALPOST_ID is 'Journalpostid i Doksys';

comment on column BREV_SPORING.DOKUMENT_ID is 'Dokumentid i Doksys';

comment on column BREV_SPORING.BREV_TYPE is 'Bestilt brev type';

create index IDX_BREV_SPORING
    on BREV_SPORING (BEHANDLING_ID);

create index IDX_BREV_SPORING_1
    on BREV_SPORING (BREV_TYPE);

create table OKO_XML_MOTTATT_ARKIV
(
    ID            NUMBER(19)                              not null
        constraint PK_OKO_XML_MOTATT_ARKIV
        primary key,
    MELDING       CLOB                                    not null,
    OPPRETTET_AV  VARCHAR2(200 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)       default SYSTIMESTAMP not null,
    ENDRET_AV     VARCHAR2(200 char),
    ENDRET_TID    TIMESTAMP(3)
);

comment on table OKO_XML_MOTTATT_ARKIV is 'Tabell for å arkivere gamle kravgrunnlag som ikke finnes i Økonomi.';

comment on column OKO_XML_MOTTATT_ARKIV.ID is 'Primary key';

comment on column OKO_XML_MOTTATT_ARKIV.MELDING is 'Gammel kravgrunnlag XML';

create table VERGE
(
    ID            NUMBER(19)                             not null
        constraint PK_VERGE
        primary key,
    AKTOER_ID     VARCHAR2(50 char),
    GYLDIG_FOM    DATE                                   not null,
    GYLDIG_TOM    DATE                                   not null,
    VERGE_TYPE    VARCHAR2(100 char)                     not null,
    ORGNR         VARCHAR2(100 char),
    NAVN          VARCHAR2(1000 char)                    not null,
    KILDE         VARCHAR2(50 char)                      not null,
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    BEGRUNNELSE   VARCHAR2(4000 char)
);

comment on table VERGE is 'Informasjon om verge';

comment on column VERGE.ID is 'Primary Key';

comment on column VERGE.AKTOER_ID is 'AktørId av verge person';

comment on column VERGE.GYLDIG_FOM is 'Hvis fullmakt er begrenset i periode, dato for når fullmakten er gyldig fra';

comment on column VERGE.GYLDIG_TOM is 'Hvis fullmakt er begrenset i periode, dato for når fullmakten er gyldig til';

comment on column VERGE.VERGE_TYPE is 'Type verge';

comment on column VERGE.ORGNR is 'Vergens organisasjonsnummer';

comment on column VERGE.NAVN is 'Navn på vergen, som tastet inn av saksbehandler';

comment on column VERGE.KILDE is 'Opprinnelsen av verge.Enten Fpsak hvis det kopierte fra fpsak eller fptilbake';

comment on column VERGE.BEGRUNNELSE is 'Begrunnelse for verge';

create table GR_VERGE
(
    ID            NUMBER(19)                             not null
        constraint PK_GR_VERGE
        primary key,
    BEHANDLING_ID NUMBER(19)                             not null
        constraint FK_GR_VERGE_1
        references BEHANDLING,
    VERGE_ID      NUMBER(19)                             not null
        constraint FK_GR_VERGE_2
        references VERGE,
    AKTIV         VARCHAR2(1 char)                       not null
        constraint CHK_AKTIV_VERGE
        check (AKTIV IN ('J', 'N')),
    OPPRETTET_AV  VARCHAR2(20 char) default 'VL'         not null,
    OPPRETTET_TID TIMESTAMP(3)      default systimestamp not null,
    ENDRET_AV     VARCHAR2(20 char),
    ENDRET_TID    TIMESTAMP(3),
    VERSJON       NUMBER(19)        default 0            not null
);

comment on table GR_VERGE is 'Aggregate tabell for å lagre verge';

comment on column GR_VERGE.ID is 'Primary Key';

comment on column GR_VERGE.BEHANDLING_ID is 'FK: Referanse til behandling';

comment on column GR_VERGE.VERGE_ID is 'FK:VERGE';

comment on column GR_VERGE.AKTIV is 'Angir status av verge';

create index IDX_GR_VERGE_1
    on GR_VERGE (BEHANDLING_ID);

create index IDX_GR_VERGE_2
    on GR_VERGE (VERGE_ID);

