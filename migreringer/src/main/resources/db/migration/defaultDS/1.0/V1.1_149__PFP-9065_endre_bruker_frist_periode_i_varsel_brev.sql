INSERT INTO KONFIG_VERDI_KODE (KODE, NAVN, KONFIG_GRUPPE, KONFIG_TYPE, BESKRIVELSE) VALUES ('brukertilbakemelding.venter.frist.lengde', 'Frist - Bruker tilbakemelding venter', 'INGEN', 'PERIOD', 'Sett behandling på vent');

INSERT INTO KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) VALUES (SEQ_KONFIG_VERDI.NEXTVAL,'brukertilbakemelding.venter.frist.lengde','INGEN','P2W',to_date('01.01.2016','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));
