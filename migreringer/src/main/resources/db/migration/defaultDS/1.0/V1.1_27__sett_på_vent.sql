insert into KONFIG_VERDI_KODE (kode, navn, konfig_gruppe, konfig_type, beskrivelse) values ('frist.brukerrespons.varsel', 'Hvor lenge behandlingen venter på brukerrespons', 'INGEN', 'PERIOD', 'Sett behandling på vent  (i en angitt periode, eks. P2W = 2 uker');
insert into KONFIG_VERDI (id, konfig_kode, konfig_gruppe, konfig_verdi) values (SEQ_KONFIG_VERDI.nextval, 'frist.brukerrespons.varsel', 'INGEN', 'P4W');

insert into AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, BESKRIVELSE, VILKAR_TYPE, TOTRINN_BEHANDLING_DEFAULT, AKSJONSPUNKT_TYPE, FRIST_PERIODE, TILBAKEHOPP_VED_GJENOPPTAKELSE, LAG_UTEN_HISTORIKK, SKJERMLENKE_TYPE )
values ('7001','Venter på tilbakemelding fra bruker','VARSELSTEG.UT','Venter på tilbakemelding fra bruker', '-','N','AUTO','PT4W','N','J','-');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, beskrivelse)
VALUES(seq_kodeliste.nextval, 'VENT_AARSAK', 'VENT_PÅ_BRUKERTILBAKEMELDING','Venter på tilbakemelding fra bruker','Venter på tilbakemelding fra bruker');
