INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE)
  VALUES ('brev.sendInnhentDokumentasjon','Sender innhent-dokumentasjonbrev til bruker','3','Sender innhent-dokumentasjonbrev til bruker når dokumentasjon mangler');

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM)
    VALUES (seq_kodeliste.nextval,'BREV_TYPE','INNHENT_DOKUMENTASJON',null, 'Innhentdokumentasjonbrev',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
