alter table mottaker_varsel_respons rename column saksnummer to behandling_id;
comment on column mottaker_varsel_respons.behandling_id is 'behandlingen responsen hører til';