-- legger til oversettelser på fagsak_ytelse
update kodeliste_navn_i18n set navn = 'Engangsstønad' where kl_kodeverk = 'FAGSAK_YTELSE' and kl_kode = 'ES';
update kodeliste_navn_i18n set navn = 'Foreldrepenger' where kl_kodeverk = 'FAGSAK_YTELSE' and kl_kode = 'FP';
update kodeliste_navn_i18n set navn = 'Ikke definert' where kl_kodeverk = 'FAGSAK_YTELSE' and kl_kode = '-';

-- legger til svangerskapspenger til kodeverk
insert into kodeliste (id, kodeverk, kode, beskrivelse, gyldig_fom, gyldig_tom) values (seq_kodeliste.nextval, 'FAGSAK_YTELSE', 'SVP', 'Svangerskapspenger', to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
insert into kodeliste_navn_i18n (id, kl_kodeverk, kl_kode, sprak, navn) values (seq_kodeliste_navn_i18n.nextval, 'FAGSAK_YTELSE', 'SVP', 'NB', 'Svangerskapspenger');

-- fjerner fagsak_ytelse som ikke skal brukes
delete from kodeliste_navn_i18n where kl_kodeverk = 'FAGSAK_YTELSE' and kl_kode = 'ENDRING_FP';
delete from kodeliste where kodeverk = 'FAGSAK_YTELSE' and kode = 'ENDRING_FP';
