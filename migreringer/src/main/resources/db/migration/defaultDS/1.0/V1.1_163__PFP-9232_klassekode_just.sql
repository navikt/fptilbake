INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE,GYLDIG_FOM,GYLDIG_TOM)
VALUES (SEQ_KODELISTE.NEXTVAL,'KLASSE_KODE','KL_KODE_JUST_KORTTID',null,'KL_KODE_JUST_KORTTID',to_date('01.01.2000','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));

INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN,OPPRETTET_AV,OPPRETTET_TID,ENDRET_AV,ENDRET_TID)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'KLASSE_KODE','KL_KODE_JUST_KORTTID','NB','KL_KODE_JUST_KORTTID','VL',to_date('31.01.2019','DD.MM.RRRR'),null,null);
