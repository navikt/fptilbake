ALTER TABLE EKSTERN_BEHANDLING ADD HENVISNING VARCHAR2(30 CHAR);
CREATE INDEX IDX_EKSTERN_BEHANDLING_4 ON EKSTERN_BEHANDLING (HENVISNING);
COMMENT ON COLUMN EKSTERN_BEHANDLING.HENVISNING IS 'Henvisning/referanse. Peker på referanse-feltet i kravgrunnlaget, og kommer opprinnelig fra fagsystemet. For fptilbake er den lik fpsak.behandlingId. For k9-tilbake er den lik base64(bytes(behandlingUuid))';

ALTER TABLE OKO_XML_MOTTATT ADD HENVISNING VARCHAR2(30 CHAR);
CREATE INDEX IDX_OKO_XML_MOTTATT_2 ON OKO_XML_MOTTATT (HENVISNING);
CREATE INDEX IDX_OKO_XML_MOTTATT_3 ON OKO_XML_MOTTATT (SAKSNUMMER);
COMMENT ON COLUMN OKO_XML_MOTTATT.HENVISNING IS 'Henvisning/referanse. Peker på referanse-feltet i kravgrunnlaget, og kommer opprinnelig fra fagsystemet. For fptilbake er den lik fpsak.behandlingId. For k9-tilbake er den lik base64(bytes(behandlingUuid))';

