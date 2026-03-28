package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker.bigquery.tabell;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;

import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryRecord;
import no.nav.k9.felles.integrasjon.bigquery.tabell.BigQueryTabellDefinisjon;

public final class ProsessTaskFeilTabell implements BigQueryTabellDefinisjon {

    public static final ProsessTaskFeilTabell INSTANCE = new ProsessTaskFeilTabell();
    private static final String YTELSE_TYPE = "ytelse_type";
    private static final String PROSESS_TASK_TYPE = "prosess_task_type";
    private static final String STATUS = "status";
    private static final String TASK_ID = "task_id";
    private static final String SAKSNUMMER = "saksnummer";
    private static final String BLOKKERT_AV = "blokkert_av";
    private static final String GRUPPE_SEKVENSNR = "gruppe_sekvensnr";
    private static final String OPPRETTET_TIDSPUNKT = "opprettet_tidspunkt";
    private static final String SISTE_KJOERT = "siste_kjoert";
    private static final String TIME = "time";

    private ProsessTaskFeilTabell() {
    }

    @Override
    public String getTabellNavn() {
        return "prosess_task_feil_log_v1";
    }

    @Override
    public Schema getSchema() {
        return Schema.of(
            BigQueryTabellDefinisjon.newNullableField(YTELSE_TYPE, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newRequiredField(PROSESS_TASK_TYPE, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newRequiredField(STATUS, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newRequiredField(TASK_ID, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newNullableField(SAKSNUMMER, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newNullableField(BLOKKERT_AV, StandardSQLTypeName.STRING),
            BigQueryTabellDefinisjon.newNullableField(GRUPPE_SEKVENSNR, StandardSQLTypeName.BIGNUMERIC),
            BigQueryTabellDefinisjon.newRequiredField(OPPRETTET_TIDSPUNKT, StandardSQLTypeName.TIMESTAMP),
            BigQueryTabellDefinisjon.newNullableField(SISTE_KJOERT, StandardSQLTypeName.TIMESTAMP),
            BigQueryTabellDefinisjon.newRequiredField(TIME, StandardSQLTypeName.TIMESTAMP)
        );
    }

    @Override
    public Function<BigQueryRecord, Map<String, ?>> getRowMapper(Instant now) {
        return record -> {
            var r = (ProsessTaskFeilRecord) record;
            var map = new HashMap<String, Object>(); // Kan ikke bruke Map.ofEntries pga null verdier
            map.put(YTELSE_TYPE, r.ytelseType());
            map.put(PROSESS_TASK_TYPE, r.prosessTaskType());
            map.put(STATUS, r.status());
            map.put(TASK_ID, r.taskId());
            map.put(SAKSNUMMER, r.saksnummer());
            map.put(BLOKKERT_AV, r.blokkertAv());
            map.put(GRUPPE_SEKVENSNR, r.gruppeSekvensnr());
            map.put(OPPRETTET_TIDSPUNKT, r.opprettetTid());
            map.put(SISTE_KJOERT, r.sistKjortTid());
            map.put(TIME, BigQueryTabellDefinisjon.avrund(now).toString());
            return map;
        };
    }

    @Override
    public boolean skalTømmeFørSkriv() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof BigQueryTabellDefinisjon other
            && getTabellNavn().equals(other.getTabellNavn()));
    }

    @Override
    public int hashCode() {
        return getTabellNavn().hashCode();
    }
}
