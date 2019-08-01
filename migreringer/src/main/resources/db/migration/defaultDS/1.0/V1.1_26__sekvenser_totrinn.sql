CREATE TABLE VURDER_AARSAK_TTVURDERING (
  id              NUMBER(19)                        NOT NULL,
  aarsak_type     VARCHAR2(100 CHAR)                    NOT NULL,
  KL_AARSAK_TYPE  VARCHAR2(100 CHAR) AS ('VURDER_AARSAK'),
  totrinnsvurdering_id NUMBER(19)                        NOT NULL,
  opprettet_av    VARCHAR2(20 CHAR) DEFAULT 'VL'    NOT NULL,
  opprettet_tid   TIMESTAMP(3) DEFAULT systimestamp NOT NULL,
  endret_av       VARCHAR2(20 CHAR),
  endret_tid      TIMESTAMP(3),
  CONSTRAINT PK_VURDER_AARSAK_TTVURDERING PRIMARY KEY (id),
  CONSTRAINT FK_VURDER_AARSAK_TTVURDERING_1 FOREIGN KEY (totrinnsvurdering_id) REFERENCES TOTRINNSVURDERING (id),
  CONSTRAINT FK_VURDER_AARSAK_TTVURDERING_2 FOREIGN KEY (AARSAK_TYPE, KL_AARSAK_TYPE) REFERENCES KODELISTE(KODE, KODEVERK)
);

CREATE SEQUENCE SEQ_VURDER_AARSAK_TTVURDERING MINVALUE 3000000 START WITH 3000000 INCREMENT BY 50 NOCACHE NOCYCLE;

COMMENT ON TABLE VURDER_AARSAK_TTVURDERING IS 'Årsaken til at aksjonspunkt må vurderes på nytt';

CREATE INDEX IDX_VURDER_AARSAK ON VURDER_AARSAK_TTVURDERING(totrinnsvurdering_id);
CREATE INDEX IDX_VURDER_AARSAK_2 ON VURDER_AARSAK_TTVURDERING(AARSAK_TYPE);
