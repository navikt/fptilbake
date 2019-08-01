insert into vurderingspunkt_def (kode, behandling_steg, vurderingspunkt_type, navn)
values ('TBKGSTEG.UT', 'TBKGSTEG', 'UT', 'Tilbakekrevingsgrunnlag - Utgang');

update aksjonspunkt_def
set frist_periode = 'P4W', tilbakehopp_ved_gjenopptakelse = 'J', vurderingspunkt = 'TBKGSTEG.UT'
where kode = '7002' and vurderingspunkt = 'TBKGSTEG.INN';

insert into konfig_verdi_kode (kode, navn, konfig_gruppe, konfig_type, beskrivelse)
values ('frist.grunnlag.tbkg', 'Frist - Behandling venter på grunnlag fra økonomi', 'INGEN', 'PERIOD', 'Sett behandling på vent (i en angitt periode, eks. P3W = 3 uker');

insert into konfig_verdi (id, konfig_kode,konfig_gruppe,konfig_verdi,gyldig_fom,gyldig_tom)
values (seq_kodeliste.nextval, 'frist.grunnlag.tbkg', 'INGEN', 'P4W', to_date('01.01.2016', 'DD.MM.RRRR'),to_date('31.12.9999', 'DD.MM.RRRR'));