ALTER TABLE EKSTERN_BEHANDLING MODIFY ( EKSTERN_ID NULL);
ALTER TABLE EKSTERN_BEHANDLING MODIFY ( HENVISNING NOT NULL);
ALTER TABLE EKSTERN_BEHANDLING ADD CONSTRAINT UIDX_EKSTERN_BEHANDLING_2 unique(INTERN_ID, HENVISNING);
ALTER TABLE EKSTERN_BEHANDLING DROP CONSTRAINT UIDX_EKSTERN_BEHANDLING_1;
