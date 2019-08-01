/** Behandlingssteg **/
Insert into BEHANDLING_STEG_TYPE (KODE,NAVN,BEHANDLING_STATUS_DEF,BESKRIVELSE)
values ('VARSELSTEG','Varsel om tilbakekreving','UTRED','Vurdere om varsel om tilbakekreving skal sendes til søker.');

Insert into BEHANDLING_STEG_TYPE (KODE,NAVN,BEHANDLING_STATUS_DEF,BESKRIVELSE)
values ('FORVEDSTEG','Foreslå vedtak','UTRED','Totrinnskontroll av behandling. Går rett til fatte vedtak dersom behandlingen ikke krever totrinnskontroll.');

Insert into BEHANDLING_STEG_TYPE (KODE,NAVN,BEHANDLING_STATUS_DEF,BESKRIVELSE)
values ('FVEDSTEG','Fatte Vedtak','FVED','Fatte vedtak for en behandling.');

Insert into BEHANDLING_STEG_TYPE (KODE,NAVN,BEHANDLING_STATUS_DEF,BESKRIVELSE)
values ('IVEDSTEG','Iverksett Vedtak','IVED','Iverksett vedtak fra en behandling.  Forutsetter at et vedtak er fattet');


/** Vurderingspunkt **/
Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('VARSELSTEG.INN','VARSELSTEG','INN','Varsel om tilbakekreving - Inngang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('VARSELSTEG.UT','VARSELSTEG','UT','Varsel om tilbakekreving - Utgang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('FORVEDSTEG.INN','FORVEDSTEG','INN','Foreslå vedtak - Inngang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('FORVEDSTEG.UT','FORVEDSTEG','UT','Foreslå vedtak - Utgang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('FVEDSTEG.INN','FVEDSTEG','INN','Fatter vedtak - Inngang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('FVEDSTEG.UT','FVEDSTEG','UT','Fatter vedtak - Utgang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('IVEDSTEG.INN','IVEDSTEG','INN','Iverksett vedtak - Inngang');

Insert into VURDERINGSPUNKT_DEF (KODE,BEHANDLING_STEG,VURDERINGSPUNKT_TYPE,NAVN)
values ('IVEDSTEG.UT','IVEDSTEG','UT','Iverksett vedtak - Utgang');


/** Aksjonspunktdefinisjoner **/
Insert into AKSJONSPUNKT_DEF (KODE,NAVN,VURDERINGSPUNKT,BESKRIVELSE,TOTRINN_BEHANDLING_DEFAULT,AKSJONSPUNKT_TYPE,TILBAKEHOPP_VED_GJENOPPTAKELSE,LAG_UTEN_HISTORIKK, SKJERMLENKE_TYPE)
values ('5001','Varsel om tilbakekreving','VARSELSTEG.UT','Send varsel om tilbakekreving.','N','MANU','N','N', '-');

