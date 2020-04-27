alter table prosess_task drop constraint chk_prosess_task_status;
alter table prosess_task add constraint CHK_PROSESS_TASK_STATUS check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG', 'KJOERT'));


