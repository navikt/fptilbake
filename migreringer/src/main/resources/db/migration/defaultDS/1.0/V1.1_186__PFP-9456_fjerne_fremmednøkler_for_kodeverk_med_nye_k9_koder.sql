-- FagOmrådeKode
alter table KRAV_GRUNNLAG_431 drop constraint FK_KRAV_GRUNNLAG_431_2;
alter table KRAV_VEDTAK_STATUS_437 drop constraint FK_KRAV_VEDTAK_STATUS_437_2;

-- FagsakYtelseType
alter table FAGSAK drop constraint FK_FAGSAK_3;

-- HendelseType & HendelseUnderType
alter table FAKTA_FEILUTBETALING_PERIODE drop constraint FK_FAKTA_FEILUT_PERIODE_2;
alter table FAKTA_FEILUTBETALING_PERIODE drop constraint FK_FAKTA_FEILUT_PERIODE_3;

-- Diverse?
alter table HISTORIKKINNSLAG_FELT drop constraint FK_HISTORIKKINNSLAG_FELT_4
