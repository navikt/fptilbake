INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE) VALUES (seq_kodeliste.nextval,'HENDELSE_TYPE','MEDLEMSKAP',null,'§14-2 Medlemskap');
INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','NB','§14-2 Medlemskap');

INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE) VALUES (seq_kodeliste.nextval,'HENDELSE_UNDERTYPE','IKKE_LOVLIG_OPPHOLD',null,'Ikke lovlig opphold, ikke oppholdstillatelse');
INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HENDELSE_UNDERTYPE','IKKE_LOVLIG_OPPHOLD','NB','Ikke lovlig opphold');
INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE) VALUES (seq_kodeliste.nextval,'HENDELSE_UNDERTYPE','IKKE_OPPHOLDSRETT_EØS',null,'Ikke oppholdsrett etter EØS-avtalen');
INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HENDELSE_UNDERTYPE','IKKE_OPPHOLDSRETT_EØS','NB','Ikke oppholdsrett EØS');
INSERT INTO KODELISTE (ID,KODEVERK,KODE,OFFISIELL_KODE,BESKRIVELSE) VALUES (seq_kodeliste.nextval,'HENDELSE_UNDERTYPE','IKKE_BOSATT',null,'Ikke bosatt');
INSERT INTO KODELISTE_NAVN_I18N (ID,KL_KODEVERK,KL_KODE,SPRAK,NAVN) VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL,'HENDELSE_UNDERTYPE','IKKE_BOSATT','NB','Ikke bosatt');

UPDATE KODELISTE SET EKSTRA_DATA=0 WHERE KODEVERK='HENDELSE_UNDERTYPE' AND KODE='UTVANDRET';
UPDATE KODELISTE SET EKSTRA_DATA=1 WHERE KODEVERK='HENDELSE_UNDERTYPE' AND KODE='IKKE_BOSATT';
UPDATE KODELISTE SET EKSTRA_DATA=2 WHERE KODEVERK='HENDELSE_UNDERTYPE' AND KODE='IKKE_OPPHOLDSRETT_EØS';
UPDATE KODELISTE SET EKSTRA_DATA=3 WHERE KODEVERK='HENDELSE_UNDERTYPE' AND KODE='IKKE_LOVLIG_OPPHOLD';
UPDATE KODELISTE SET EKSTRA_DATA=4 WHERE KODEVERK='HENDELSE_UNDERTYPE' AND KODE='MEDLEM_I_ANNET_LAND';

INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','HENDELSE_UNDERTYPE','UTVANDRET');
INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','HENDELSE_UNDERTYPE','IKKE_LOVLIG_OPPHOLD');
INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','HENDELSE_UNDERTYPE','MEDLEM_I_ANNET_LAND');
INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','HENDELSE_UNDERTYPE','IKKE_OPPHOLDSRETT_EØS');
INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'HENDELSE_TYPE','MEDLEMSKAP','HENDELSE_UNDERTYPE','IKKE_BOSATT');

INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'FAGSAK_YTELSE','FP','HENDELSE_TYPE','MEDLEMSKAP');
INSERT INTO KODELISTE_RELASJON (ID,KODEVERK1,KODE1,KODEVERK2,KODE2) VALUES (SEQ_KODELISTE_RELASJON.NEXTVAL,'FAGSAK_YTELSE','SVP','HENDELSE_TYPE','MEDLEMSKAP');

update FEILUTBETALING_PERIODE_AARSAK set UNDER_AARSAK_KODEVERK='IKKE_LOVLIG_OPPHOLD' where UNDER_AARSAK_KODEVERK in ('IKKE_OPPHOLDSTILLATLSE', 'SVP_IKKE_OPPHOLDSTILLATLSE');
update FEILUTBETALING_PERIODE_AARSAK set UNDER_AARSAK_KODEVERK='UTVANDRET' where UNDER_AARSAK_KODEVERK in ('SVP_UTVANDRET');
update FEILUTBETALING_PERIODE_AARSAK set UNDER_AARSAK_KODEVERK='MEDLEM_I_ANNET_LAND' where UNDER_AARSAK_KODEVERK in ('SVP_MEDLEM_I_ANNET_LAND');
update FEILUTBETALING_PERIODE_AARSAK set UNDER_AARSAK_KODEVERK='IKKE_OPPHOLDSRETT_EØS' where UNDER_AARSAK_KODEVERK in ('IKKE_JOBBET', 'SVP_IKKE_JOBBET');
update FEILUTBETALING_PERIODE_AARSAK set UNDER_AARSAK_KODEVERK='IKKE_BOSATT' where UNDER_AARSAK_KODEVERK in ('MER_OPPHOLD', 'SVP_MER_OPPHOLD');
update FEILUTBETALING_PERIODE_AARSAK set AARSAK_KODEVERK = 'MEDLEMSKAP' where AARSAK_KODEVERK in ('MEDLEMSKAP_TYPE', 'SVP_MEDLEMSKAP');

delete KODELISTE_NAVN_I18N where kl_kodeverk='HENDELSE_UNDERTYPE' and kl_kode in (select kode2 from kodeliste_relasjon where kodeverk1='HENDELSE_TYPE' and kode1='SVP_MEDLEMSKAP' and kodeverk2='HENDELSE_UNDERTYPE');
delete KODELISTE_NAVN_I18N where kl_kodeverk='HENDELSE_UNDERTYPE' and kl_kode in (select kode2 from kodeliste_relasjon where kodeverk1='HENDELSE_TYPE' and kode1='MEDLEMSKAP_TYPE' and kodeverk2='HENDELSE_UNDERTYPE');
delete kodeliste_relasjon where kodeverk1='HENDELSE_TYPE' and kode1='SVP_MEDLEMSKAP';
delete kodeliste_relasjon where kodeverk1='HENDELSE_TYPE' and kode1='MEDLEMSKAP_TYPE';
delete kodeliste where kodeverk='HENDELSE_UNDERTYPE' and kode not in (select kode2 from kodeliste_relasjon where kodeverk1='HENDELSE_TYPE');
delete kodeliste_relasjon where kodeverk1='FAGSAK_YTELSE' and kode1='SVP' and kodeverk2='HENDELSE_TYPE' and kode2 = 'SVP_MEDLEMSKAP';
delete kodeliste_relasjon where kodeverk1='FAGSAK_YTELSE' and kode1='FP' and kodeverk2='HENDELSE_TYPE' and kode2 = 'MEDLEMSKAP_TYPE';
delete kodeliste_navn_i18n where kl_kodeverk='HENDELSE_TYPE' and kl_kode not in (select kode2 from kodeliste_relasjon where kodeverk1='FAGSAK_YTELSE');
delete kodeliste where kodeverk='HENDELSE_TYPE' and kode not in (select kode2 from kodeliste_relasjon where kodeverk1='FAGSAK_YTELSE');

