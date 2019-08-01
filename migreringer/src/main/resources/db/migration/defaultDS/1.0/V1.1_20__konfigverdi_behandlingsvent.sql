INSERT INTO KONFIG_VERDI_KODE (KODE, NAVN, KONFIG_GRUPPE, KONFIG_TYPE, BESKRIVELSE) VALUES ('behandling.venter.frist.lengde', 'Frist - Behandling venter', 'INGEN', 'PERIOD', 'Sett behandling på vent (i en angitt periode, eks. P3W = 3 uker');
INSERT INTO KONFIG_VERDI (ID,KONFIG_KODE,KONFIG_GRUPPE,KONFIG_VERDI,GYLDIG_FOM,GYLDIG_TOM) VALUES ('100000','behandling.venter.frist.lengde','INGEN','P3W',to_date('01.01.2016','DD.MM.RRRR'),to_date('31.12.9999','DD.MM.RRRR'));