ALTER TABLE FAGSAK ADD ekstern_fagsak_id NUMBER(19,0);
ALTER TABLE FAGSAK ADD ekstern_fagsak_system VARCHAR2(100 CHAR) DEFAULT '-' NOT NULL;
ALTER TABLE FAGSAK ADD kl_ekstern_fagsak_system VARCHAR2(100 CHAR) GENERATED ALWAYS AS ('FAGSYSTEM') VIRTUAL VISIBLE;

ALTER TABLE FAGSAK ADD CONSTRAINT FK_FAGSAK_3 FOREIGN KEY (ekstern_fagsak_system, kl_ekstern_fagsak_system) REFERENCES KODELISTE (KODE, KODEVERK);

CREATE INDEX IDX_FAGSAK_3 ON FAGSAK (ekstern_fagsak_system);

COMMENT ON COLUMN FAGSAK.ekstern_fagsak_id IS 'Referanse til fagsak i eksternt system';
COMMENT ON COLUMN FAGSAK.ekstern_fagsak_system IS 'System som ekstern_fagsak_id refererer til';
COMMENT ON COLUMN FAGSAK.kl_ekstern_fagsak_system IS 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';