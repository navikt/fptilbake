drop index idx_behandling_resultat_3;
drop index idx_behandling_resultat_4;
drop index idx_behandling_resultat_6;
drop index idx_behandling_resultat_7;

alter table behandling_resultat drop constraint fk_behandling_resultat_6;
alter table behandling_resultat drop constraint fk_behandling_resultat_81;
alter table behandling_resultat drop constraint fk_behandling_resultat_82;
alter table behandling_resultat drop constraint fk_behandling_resultat_83;

alter table behandling_resultat drop column kl_konsekvens_for_ytelsen;
alter table behandling_resultat drop column konsekvens_for_ytelsen;
alter table behandling_resultat drop column avslag_arsak;
alter table behandling_resultat drop column avslag_arsak_fritekst;
alter table behandling_resultat drop column kl_avslagsarsak;
alter table behandling_resultat drop column retten_til;
alter table behandling_resultat drop column kl_retten_til;
alter table behandling_resultat drop column vedtaksbrev;
alter table behandling_resultat drop column kl_vedtaksbrev;
alter table behandling_resultat drop column overskrift;
alter table behandling_resultat drop column fritekstbrev;
