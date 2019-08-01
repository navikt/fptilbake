update aksjonspunkt_def
set lag_uten_historikk = 'N'
where lag_uten_historikk = 'J' and (kode = '7001' or kode = '7002');