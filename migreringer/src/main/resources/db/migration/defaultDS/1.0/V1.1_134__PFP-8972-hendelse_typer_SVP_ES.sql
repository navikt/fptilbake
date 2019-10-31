--felles
update kodeliste set ekstra_data = '10' where kodeverk = 'HENDELSE_TYPE' and kode = 'MEDLEMSKAP';
update kodeliste set ekstra_data = '500' where kodeverk = 'HENDELSE_TYPE' and kode = 'OKONOMI_FEIL';

--fp
update kodeliste set ekstra_data = '20' where kodeverk = 'HENDELSE_TYPE' and kode = 'OPPTJENING_TYPE';
update kodeliste set ekstra_data = '30' where kodeverk = 'HENDELSE_TYPE' and kode = 'BEREGNING_TYPE';
update kodeliste set ekstra_data = '40' where kodeverk = 'HENDELSE_TYPE' and kode = 'STONADSPERIODEN_TYPE';
update kodeliste set ekstra_data = '50' where kodeverk = 'HENDELSE_TYPE' and kode = 'UTTAK_GENERELT_TYPE';
update kodeliste set ekstra_data = '60' where kodeverk = 'HENDELSE_TYPE' and kode = 'UTTAK_UTSETTELSE_TYPE';
update kodeliste set ekstra_data = '70' where kodeverk = 'HENDELSE_TYPE' and kode = 'UTTAK_KVOTENE_TYPE';
update kodeliste set ekstra_data = '80' where kodeverk = 'HENDELSE_TYPE' and kode = 'VILKAAR_GENERELLE_TYPE';
update kodeliste set ekstra_data = '90' where kodeverk = 'HENDELSE_TYPE' and kode = 'KUN_RETT_TYPE';
update kodeliste set ekstra_data = '100' where kodeverk = 'HENDELSE_TYPE' and kode = 'UTTAK_ALENEOMSORG_TYPE';
update kodeliste set ekstra_data = '110' where kodeverk = 'HENDELSE_TYPE' and kode = 'UTTAK_GRADERT_TYPE';
update kodeliste set ekstra_data = '999' where kodeverk = 'HENDELSE_TYPE' and kode = 'FP_ANNET_HENDELSE_TYPE';

--es
update kodeliste set ekstra_data = '20' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_ADOPSJONSVILKAARET_TYPE';
update kodeliste set ekstra_data = '30' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_FODSELSVILKAARET_TYPE';
update kodeliste set ekstra_data = '40' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_FORELDREANSVAR_TYPE';
update kodeliste set ekstra_data = '50' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_OMSORGSVILKAAR_TYPE';
update kodeliste set ekstra_data = '60' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_FORELDREANSVAR_FAR_TYPE';
update kodeliste set ekstra_data = '70' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_RETT_PAA_FORELDREPENGER_TYPE';
update kodeliste set ekstra_data = '500' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_FEIL_UTBETALING_TYPE';
update kodeliste set ekstra_data = '999' where kodeverk = 'HENDELSE_TYPE' and kode = 'ES_ANNET_TYPE';

update kodeliste_navn_i18n set navn = '§14-17 1. ledd Adopsjonsvilkåret' where kl_kode = 'ES_ADOPSJONSVILKAARET_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-17 1. ledd Fødselsvilkåret' where kl_kode = 'ES_FODSELSVILKAARET_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-17 2. ledd Foreldreansvar ' where kl_kode = 'ES_FORELDREANSVAR_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-17 3. ledd Omsorgsvilkår ved mors død' where kl_kode = 'ES_OMSORGSVILKAAR_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-17 4. ledd Foreldreansvar far' where kl_kode = 'ES_FORELDREANSVAR_FAR_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = 'Rett på foreldrepenger etter klage' where kl_kode = 'ES_RETT_PAA_FORELDREPENGER_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';

--svp
update kodeliste set ekstra_data = '20' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_FAKTA_TYPE';
update kodeliste set ekstra_data = '30' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_ARBEIDSGIVERS_FORHOLD_TYPE';
update kodeliste set ekstra_data = '40' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_ARBEIDSFORHOLD_TYPE';
update kodeliste set ekstra_data = '50' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_OPPTJENING_TYPE';
update kodeliste set ekstra_data = '60' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_BEREGNING_TYPE';
update kodeliste set ekstra_data = '70' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_UTTAK_TYPE';
update kodeliste set ekstra_data = '80' where kodeverk = 'HENDELSE_TYPE' and kode = 'OPPHØR';
update kodeliste set ekstra_data = '999' where kodeverk = 'HENDELSE_TYPE' and kode = 'SVP_ANNET_TYPE';

update kodeliste set ekstra_data = '10' where kodeverk = 'HENDELSE_UNDERTYPE' and kode = 'SVP_IKKE_ARBEID';
update kodeliste set ekstra_data = '20' where kodeverk = 'HENDELSE_UNDERTYPE' and kode = 'SVP_INNTEKT_IKKE_TAP';
update kodeliste set ekstra_data = '30' where kodeverk = 'HENDELSE_UNDERTYPE' and kode = 'SVP_INNTEKT_UNDER';

update kodeliste_navn_i18n set navn = '§14-4 Fakta om svangerskap' where kl_kode = 'SVP_FAKTA_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-4 1. ledd Arbeidsgivers forhold' where kl_kode = 'SVP_ARBEIDSGIVERS_FORHOLD_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-4 Arbeidsforhold' where kl_kode = 'SVP_ARBEIDSFORHOLD_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-4 3. ledd Opptjening/ inntekt' where kl_kode = 'SVP_OPPTJENING_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-4 5. ledd Beregning ' where kl_kode = 'SVP_BEREGNING_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';
update kodeliste_navn_i18n set navn = '§14-4 Uttak' where kl_kode = 'SVP_UTTAK_TYPE' and kl_kodeverk = 'HENDELSE_TYPE';

--svp: fjern svp-inntekt og flytt ikke tap av pensjonsgivende inntekt
update kodeliste_relasjon set kode1='SVP_OPPTJENING_TYPE' where kode2='SVP_INNTEKT_IKKE_TAP' and kodeverk1='HENDELSE_TYPE' and kodeverk2='HENDELSE_UNDERTYPE';
delete kodeliste_navn_i18n where kl_kodeverk='HENDELSE_TYPE' and kl_kode='SVP_INNTEKT_TYPE';
delete kodeliste_relasjon where kode2='SVP_INNTEKT_TYPE' and kodeverk2='HENDELSE_TYPE';
delete kodeliste where kodeverk='HENDELSE_TYPE' and kode='SVP_INNTEKT_TYPE';

--svp: flytt inntekt under 1/2G
update kodeliste_relasjon set kode1='SVP_OPPTJENING_TYPE' where kode2='SVP_INNTEKT_UNDER' and kodeverk1='HENDELSE_TYPE' and kodeverk2='HENDELSE_UNDERTYPE';

