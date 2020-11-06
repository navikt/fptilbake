
alter table aksjonspunkt drop column KL_AKSJONSPUNKT_STATUS;
alter table aksjonspunkt drop column KL_VENT_AARSAK;
alter table aksjonspunkt drop column KL_REAKTIVERING_STATUS;

alter table aksjonspunkt_def drop column KL_AKSJONSPUNKT_TYPE;
alter table aksjonspunkt_def drop column KL_VILKAR_TYPE;
alter table aksjonspunkt_def drop column KL_SKJERMLENKE_TYPE;

alter table behandling drop column KL_BEHANDLING_STATUS;
alter table behandling drop column KL_BEHANDLING_TYPE;

alter table behandling_resultat drop column KL_BEHANDLING_RESULTAT_TYPE;

alter table behandling_steg_tilstand drop column KL_BEHANDLING_STEG_STATUS;
alter table behandling_steg_type drop column KL_BEHANDLING_STATUS_DEF;
alter table behandling_type_steg_sekv drop column KL_BEHANDLING_TYPE;

alter table behandling_vedtak drop column KL_IVERKSETTING_STATUS;
alter table behandling_vedtak drop column KL_VEDTAK_RESULTAT_TYPE;

alter table brev_sporing drop column KL_BREV_TYPE;

alter table bruker drop column KL_SPRAAK_KODE;

alter table fagsak drop column KL_FAGSAK_STATUS;
alter table fagsak drop column KL_YTELSE_TYPE;

alter table fakta_feilutbetaling_periode drop column KL_HENDELSE_TYPE;
alter table fakta_feilutbetaling_periode drop column KL_HENDELSE_UNDERTYPE;

alter table foreldelse_periode drop column KL_FORELDELSE_VURDERING_TYPE;

alter table historikkinnslag drop column KL_HISTORIKK_AKTOER;
alter table historikkinnslag drop column KL_BRUKER_KJOENN;
alter table historikkinnslag drop column KL_HISTORIKKINNSLAG_TYPE;

--fjerner ikke fra HISTORIKKINNSLAG_FELT, da disse ser ut til å være i bruk
comment on column HISTORIKKINNSLAG_FELT.KL_NAVN is 'Hvilket kodeverk/enum som er brukt for NAVN';
comment on column HISTORIKKINNSLAG_FELT.KL_FRA_VERDI is 'Hvilket kodeverk/enum som er brukt for FRA_VERDI';
comment on column HISTORIKKINNSLAG_FELT.KL_TIL_VERDI is 'Hvilket kodeverk/enum som er brukt for TIL_VERDI';

alter table krav_grunnlag_431 drop column KL_KRAV_STATUS_KODE;
alter table krav_grunnlag_431 drop column KL_FAG_OMRAADE_KODE;
alter table krav_grunnlag_431 drop column KL_GJELDER_TYPE;

alter table KRAV_GRUNNLAG_BELOP_433 drop column KL_KLASSE_TYPE;

alter table KRAV_VEDTAK_STATUS_437 drop column KL_KRAV_STATUS_KODE;
alter table KRAV_VEDTAK_STATUS_437 drop column KL_FAG_OMRAADE_KODE;
alter table KRAV_VEDTAK_STATUS_437 drop column KL_GJELDER_TYPE;

alter table OKO_XML_SENDT drop column KL_MELDING_TYPE;

alter table VEDTAKSBREV_PERIODE drop column KL_FRITEKST_TYPE;

alter table VERGE drop column KL_VERGE_TYPE;

alter table VILKAAR_AKTSOMHET drop column KL_AKTSOMHET;
alter table VILKAAR_PERIODE drop column KL_NAV_OPPFULGT;
alter table VILKAAR_PERIODE drop column KL_VILKAAR_RESULTAT;
alter table VILKAAR_SAERLIG_GRUNN drop column KL_SAERLIG_GRUNN;

alter table VURDER_PAA_NYTT_AARSAK drop column KL_AARSAK_TYPE;

alter table VURDER_AARSAK_TTVURDERING drop column KL_AARSAK_TYPE;
COMMENT ON COLUMN VURDER_AARSAK_TTVURDERING.AARSAK_TYPE is 'Årsak til at løsning på aksjonspunkt er underkjent';
