UPDATE FAKTA_FEILUTBETALING_PERIODE SET HENDELSE_TYPE='ES_MEDLEMSKAP' where id in
(select per.id from BEHANDLING beh
inner join FAGSAK fagsak on beh.FAGSAK_ID = fagsak.ID
inner join GR_FAKTA_FEILUTBETALING fakta on beh.id = fakta.BEHANDLING_ID
inner join FAKTA_FEILUTBETALING_PERIODE per on per.fakta_feilutbetaling_id = fakta.FAKTA_FEILUTBETALING_ID
where per.HENDELSE_TYPE='MEDLEMSKAP' and fagsak.YTELSE_TYPE='ES');
