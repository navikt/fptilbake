INSERT INTO KONFIG_VERDI_KODE (KODE, NAVN, KONFIG_GRUPPE, KONFIG_TYPE, BESKRIVELSE)
VALUES ('grunnlag.alder', 'Minimumsalder på grunnlag', 'INGEN', 'PERIOD', 'Minimumsalder på grunnlag uten tilkobling eller saksbehandling');

INSERT INTO KONFIG_VERDI (ID, KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM)
VALUES (seq_kodeliste.nextval, 'grunnlag.alder', 'INGEN', 'P8W', to_date('01.01.2020', 'DD.MM.RRRR'),to_date('31.12.9999', 'DD.MM.RRRR'));

INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE) VALUES ('saksbehandling.automatisk','Automatisk saksbehandling','3','Automatisk saksbehandling');
