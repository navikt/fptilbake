INSERT INTO KODEVERK (KODE, NAVN, BESKRIVELSE, KODEVERK_EIER)
VALUES('TILBAKEKR_VIDERE_BEH', 'Kodeverk over mulige måter å behandle tilbakekreving på ifbm. feilutbetaling', 'Kodeverk over mulige måter å behandle tilbakekreving på ifbm. feilutbetaling', 'VL');

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'TILBAKEKR_VIDERE_BEH','TILBAKEKR_INFOTRYGD', 'Gjennomfør tilbakekreving i Infotrygd.', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'TILBAKEKR_VIDERE_BEH','TILBAKEKR_IGNORER', 'Avvent samordning, ingen tilbakekreving.', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'TILBAKEKR_VIDERE_BEH','TILBAKEKR_INNTREKK', 'Utfør inntrekk i fpsak.', to_date('2000-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (ID, KODEVERK, KODE, BESKRIVELSE,GYLDIG_FOM)
VALUES (seq_kodeliste.nextval, 'TILBAKEKR_VIDERE_BEH','-', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'));
