alter table VARSEL add VARSEL_FRITEKST_UTVIDET CLOB;
comment on column VARSEL.VARSEL_FRITEKST_UTVIDET is 'fritekst som brukes i varselbrev, med plass til flere tegn';
update VARSEL set VARSEL_FRITEKST_UTVIDET = FRITEKST_VARSEL;
