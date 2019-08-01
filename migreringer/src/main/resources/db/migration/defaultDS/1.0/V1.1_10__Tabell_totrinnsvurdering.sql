CREATE TABLE TOTRINNSVURDERING
(
    ID               NUMBER(19, 0)                     NOT NULL,
    BEHANDLING_ID    NUMBER(19, 0)                     NOT NULL,
    AKSJONSPUNKT_DEF VARCHAR2(50 CHAR)                 NOT NULL,
    AKTIV            VARCHAR2(1 CHAR) DEFAULT 'J'      NOT NULL,
    GODKJENT         VARCHAR2(1 CHAR)                  NOT NULL,
    BEGRUNNELSE      VARCHAR2(4000 CHAR),
    VERSJON          NUMBER(19, 0) DEFAULT 0           NOT NULL,
    OPPRETTET_AV     VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
    ENDRET_AV        VARCHAR2(20 CHAR),
    ENDRET_TID       TIMESTAMP(3),
    CONSTRAINT PK_TOTRINNSVURDERING PRIMARY KEY (ID),
    CONSTRAINT FK_TOTRINNSVURDERING_1 FOREIGN KEY (AKSJONSPUNKT_DEF) REFERENCES AKSJONSPUNKT_DEF,
    CONSTRAINT FK_TOTRINNSVURDERING_2 FOREIGN KEY (BEHANDLING_ID) REFERENCES BEHANDLING
);

CREATE SEQUENCE SEQ_TOTRINNSVURDERING MINVALUE 1 START WITH 1 INCREMENT BY 50 NOCACHE NOCYCLE;

CREATE INDEX IDX_TOTRINNSVURDERING_1 ON TOTRINNSVURDERING (AKSJONSPUNKT_DEF);
CREATE INDEX IDX_TOTRINNSVURDERING_2 ON TOTRINNSVURDERING (BEHANDLING_ID);

COMMENT ON COLUMN TOTRINNSVURDERING.GODKJENT IS 'Beslutters godkjenning';
COMMENT ON COLUMN TOTRINNSVURDERING.BEGRUNNELSE IS 'Beslutters begrunnelse';
COMMENT ON TABLE TOTRINNSVURDERING IS 'Statisk read only totrinnsvurdering som brukes til å vise vurderinger til aksjonspunkter uavhengig av status';