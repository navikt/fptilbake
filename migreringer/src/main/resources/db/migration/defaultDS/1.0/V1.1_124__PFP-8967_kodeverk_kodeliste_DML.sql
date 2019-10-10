INSERT INTO KODEVERK (KODE, NAVN, BESKRIVELSE, KODEVERK_EIER)
VALUES('MELDING_TYPE', 'Kodeverk over forskjellige melding som fptilbake sender til OS', 'Kodeverk over forskjellige melding som fptilbake sender til OS', 'VL');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'MELDING_TYPE','VEDTAK', 'TilbakekrevingsVedtak melding sendt til OS', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'MELDING_TYPE','ANNULERE_GRUNNLAG', 'Annulere grunnlag sendt til OS', to_date('2000-01-01', 'YYYY-MM-DD'));
