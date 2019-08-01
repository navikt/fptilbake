-- Aktsomhet
INSERT INTO KODEVERK (KODE,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE)
VALUES ('VURDERING','N','N','Vurdering','Kodeverk for vurderinger ut over Aktsomhet (se eget kodeverk). Brukes bare av GUI');

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'VURDERING','GOD_TRO', 'Handlet i god tro',to_date('01.01.2000','DD.MM.RRRR'));
INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'VURDERING','FORELDET', 'Foreldet',to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VURDERING', 'GOD_TRO', 'NB', 'Handlet i god tro');
INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VURDERING', 'FORELDET', 'NB', 'Foreldet');

-- VEDTAK_RESULTAT
INSERT INTO KODEVERK (KODE,KODEVERK_SYNK_NYE,KODEVERK_SYNK_EKSISTERENDE,NAVN,BESKRIVELSE)
VALUES ('VEDTAK_RESULTAT','N','N','Vedtak Resultat','Kodeverk for vedtak resultat som skal vise i vedtak');

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'VEDTAK_RESULTAT','FULL_TILBAKEBETALING', 'Full tilbakebetaling',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'VEDTAK_RESULTAT','DELVIS_TILBAKEBETALING', 'Delvis tilbakebetaling',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE (ID,KODEVERK,KODE,BESKRIVELSE,GYLDIG_FOM) VALUES (seq_kodeliste.nextval,'VEDTAK_RESULTAT','INGEN_TILBAKEBETALING', 'Ingen tilbakebetaling',
to_date('01.01.2000','DD.MM.RRRR'));

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VEDTAK_RESULTAT', 'FULL_TILBAKEBETALING',
'NB','Tilbakebetaling');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VEDTAK_RESULTAT', 'DELVIS_TILBAKEBETALING',
 'NB', 'Delvis tilbakebetaling');

 INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'VEDTAK_RESULTAT', 'INGEN_TILBAKEBETALING',
 'NB', 'Ingen tilbakebetaling');
