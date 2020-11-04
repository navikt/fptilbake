INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE,CRON_EXPRESSION)
  VALUES ('BFPT-001','Avstemming','3','Batch for å avstemme vedtak som er sendt til oppdragssystemet','0 55 6 1/1 * ? ');

INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE,CRON_EXPRESSION)
  VALUES ('BVL007','Ta behandlingen av vent','3','Batch for å ta behandling av vent når fristen har gått ut','0 0 7 ? * MON-FRI');

INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE,CRON_EXPRESSION)
  VALUES ('BFPT-002','Håndtere gamle kravgrunnlag','3','Batch for å håndtere gamle ukoblede kravgrunnlag','0 15 7 ? * MON-FRI');

INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE,CRON_EXPRESSION)
  VALUES ('BFPT-003','Automatisk Saksbehandling','3','Batch for å automatisk saksbehandle gamle saker med lavt beløp','0 30 7 ? * MON-FRI');
