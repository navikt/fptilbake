--BEHANDLING_TYPE
DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK='BEHANDLING_TYPE' AND KL_KODE IN ('BT-002','BT-003','BT-004','BT-005','BT-006');
DELETE FROM KODELISTE WHERE KODEVERK='BEHANDLING_TYPE' AND KODE IN ('BT-002','BT-003','BT-004','BT-005','BT-006');

UPDATE KODELISTE_NAVN_I18N SET NAVN='Revurdering tilbakekreving' WHERE KL_KODE='BT-008' AND KL_KODEVERK='BEHANDLING_TYPE';
UPDATE KODELISTE SET BESKRIVELSE='Revurdering tilbakekreving' WHERE KODE='BT-008' AND KODEVERK='BEHANDLING_TYPE';

--BEHANDLING_AARSAK
DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK='BEHANDLING_AARSAK';
DELETE FROM KODELISTE WHERE KODEVERK='BEHANDLING_AARSAK';

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'BEHANDLING_AARSAK','RE_KLAGE_KA', 'Revurdering etter KA-behandlet klage',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'BEHANDLING_AARSAK','RE_KLAGE_NFP', 'Revurdering NFP omgjør vedtak basert på klage',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'BEHANDLING_AARSAK','RE_VILKÅR', 'Nye opplysninger om vilkårsvurdering',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'BEHANDLING_AARSAK','RE_FORELDELSE', 'Nye opplysninger om foreldelse',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'BEHANDLING_AARSAK','-', 'Udefinert',to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE_KLAGE_KA','NB','Revurdering etter KA-behandlet klage');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE_KLAGE_NFP','NB','Revurdering NFP omgjør vedtak basert på klage');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE_VILKÅR','NB','Nye opplysninger om vilkårsvurdering');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'BEHANDLING_AARSAK', 'RE_FORELDELSE','NB','Nye opplysninger om foreldelse');

--Historikkinnslag

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID,EKSTRA_DATA)
VALUES (SEQ_KODELISTE.NEXTVAL,'HISTORIKKINNSLAG_TYPE','NY_GRUNNLAG_MOTTATT',null,null,to_date(sysdate,'DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'),'VL',
to_date(sysdate,'DD.MM.RRRR'),null,null,null);

INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HISTORIKKINNSLAG_TYPE','NY_GRUNNLAG_MOTTATT','NB','Kravgrunnlag Mottatt','VL',to_date(sysdate,'DD.MM.RRRR'),null,null);

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID,EKSTRA_DATA)
VALUES (SEQ_KODELISTE.NEXTVAL,'HISTORIKK_OPPLYSNING_TYPE','KRAVGRUNNLAG_VEDTAK_ID',null,null,to_date(sysdate,'DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'),'VL',
to_date(sysdate,'DD.MM.RRRR'),null,null,null);

INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HISTORIKK_OPPLYSNING_TYPE','KRAVGRUNNLAG_VEDTAK_ID','NB','ID','VL',to_date(sysdate,'DD.MM.RRRR'),null,null);

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID,EKSTRA_DATA)
VALUES (SEQ_KODELISTE.NEXTVAL,'HISTORIKK_OPPLYSNING_TYPE','KRAVGRUNNLAG_STATUS',null,null,to_date(sysdate,'DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'),'VL',
to_date(sysdate,'DD.MM.RRRR'),null,null,null);

INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HISTORIKK_OPPLYSNING_TYPE','KRAVGRUNNLAG_STATUS','NB','Status','VL',to_date(sysdate,'DD.MM.RRRR'),null,null);

--BEHANDLING_STEG_TYPE
UPDATE BEHANDLING_STEG_TYPE SET NAVN='Motatt kravgrunnlag fra økonomi',BESKRIVELSE='Mottat kravgrunnlag fra økonomi for tilbakekrevingsbehandling' WHERE KODE='TBKGSTEG';

INSERT INTO BEHANDLING_STEG_TYPE (KODE,NAVN,BEHANDLING_STATUS_DEF,BESKRIVELSE,OPPRETTET_AV,OPPRETTET_TID)
VALUES ('HENTGRUNNLAGSTEG','Hent grunnlag fra økonomi','UTRED','Hent kravgrunnlag fra økonomi for tilbakekrevingsrevurdering','VL',
to_timestamp('20.06.2019 14.18.01,385000000','DD.MM.RRRR HH24.MI.SSXFF'));

--BEHANDLING_TYPE_STEG_SEKV
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'HENTGRUNNLAGSTEG', 50);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'FAKTFEILUTSTEG', 70);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'VFORELDETSTEG', 80);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'VTILBSTEG', 90);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'FORVEDSTEG', 170);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'FVEDSTEG', 180);
INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR) VALUES (SEQ_BEHANDLING_TYPE_STEG_SEKV.NEXTVAL, 'BT-008', 'IVEDSTEG', 190);

--PROSESS_TASK_TYPE
INSERT INTO PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,BESKRIVELSE) VALUES ('kravgrunnlag.hent','Hent kravgrunnlag','3','Hent kravgrunnlag fra økonomi og lagret data i database');

