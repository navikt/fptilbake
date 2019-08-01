drop index UIDX_MOTTAKER_VARSEL_RESPONS_1;
alter table mottaker_varsel_respons modify behandling_id number(19,0 );
alter table mottaker_varsel_respons modify akseptert_faktagrunnlag null;
create unique index UIDX_MOTTAKER_VARSEL_RESPONS_1 on mottaker_varsel_respons (behandling_id);
alter table mottaker_varsel_respons add constraint FK_MOTTAKER_VARSEL_RESPONS_1 foreign key (behandling_id) references behandling(id) enable;
alter table mottaker_varsel_respons add kilde varchar2(100 char) not null;
comment on column mottaker_varsel_respons.kilde is 'Angir hvor responsen ble registrert';