insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feilhandtering_algoritme, beskrivelse, cron_expression)
values ('kodeverk.synch', 'Synch postnummer', 1, 'DEFAULT', 'Synkroniser postnummer månedlig', '0 0 6 1 * *');
