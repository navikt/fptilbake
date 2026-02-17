package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker.bigquery.tabell;

import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;

public record ProsessTaskFeilRecord(
    String ytelseType,
    String saksnummer,
    String taskId,
    String prosessTaskType,
    String status,
    String sistKjortTid,
    String blokkertAv,
    String opprettetTid,
    Long gruppeSekvensnr
) implements BigQueryRecord {

    @Override
    public BigQueryTabellDefinisjon tabellDefinisjon() {
        return ProsessTaskFeilTabell.INSTANCE;
    }
}
