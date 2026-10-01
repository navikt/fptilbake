update FAGSAK_PROSESS_TASK fpt
set saksnummer = (select f.saksnummer from FAGSAK f where f.id = fpt.fagsak_id)
where fpt.saksnummer is null;

create index IDX_FAGSAK_PROSESS_TASK_5
    on FAGSAK_PROSESS_TASK (SAKSNUMMER) ONLINE;

create unique index UIDX_FAGSAK_PROSESS_TASK_2
    on FAGSAK_PROSESS_TASK (SAKSNUMMER, PROSESS_TASK_ID) ONLINE;
