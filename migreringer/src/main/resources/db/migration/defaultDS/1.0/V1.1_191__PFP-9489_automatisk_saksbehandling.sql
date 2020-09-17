ALTER TABLE BEHANDLING ADD SAKSBEHANDLING_TYPE VARCHAR2(50 CHAR);

COMMENT ON COLUMN BEHANDLING.SAKSBEHANDLING_TYPE is 'Angir hvordan behandlingen saksbehandles ';

UPDATE BEHANDLING SET SAKSBEHANDLING_TYPE='ORDINÆR';

ALTER TABLE BEHANDLING MODIFY SAKSBEHANDLING_TYPE NOT NULL ENABLE;

ALTER TABLE BEHANDLING ADD CONSTRAINT CHK_SAKSBEHANDLING_TYPE CHECK (SAKSBEHANDLING_TYPE in ('ORDINÆR', 'AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP')) ENABLE;

ALTER TABLE HISTORIKKINNSLAG DROP CONSTRAINT FK_HISTORIKKINNSLAG_5;
ALTER TABLE HISTORIKKINNSLAG_FELT DROP CONSTRAINT FK_HISTORIKKINNSLAG_FELT_2;
