ALTER TABLE BEHANDLING ADD EKSTERN_BEHANDLING_ID NUMBER(19);

COMMENT ON COLUMN BEHANDLING.EKSTERN_BEHANDLING_ID IS 'Behandling ID i eksternt system';