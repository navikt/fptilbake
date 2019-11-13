insert into varselbrev_sporing (id, behandling_id, journalpost_id, dokument_id, opprettet_av, opprettet_tid, endret_av, endret_tid)
select seq_varselbrev_data.nextval, behandling_Id, journalpost_id, dokument_id, opprettet_av, opprettet_tid, endret_av, endret_tid from vedtaksbrev_sporing vs
where (vs.journalpost_id, vs.dokument_id) in (
  select journalpost_id, dokument_id from historikkinnslag_dok_link where link_tekst = 'Varselbrev Tilbakekreving'
);

delete vedtaksbrev_sporing vs
where (vs.journalpost_id, vs.dokument_id) in (
  select journalpost_id, dokument_id from historikkinnslag_dok_link where link_tekst = 'Varselbrev Tilbakekreving'
);
