drop index idx_ekstern_behandling_3;

alter table ekstern_behandling drop constraint fk_ekstern_behandling_2;
alter table ekstern_behandling drop column varseltekst;
alter table ekstern_behandling drop column fagsystem;
alter table ekstern_behandling drop column kl_fagsystem;