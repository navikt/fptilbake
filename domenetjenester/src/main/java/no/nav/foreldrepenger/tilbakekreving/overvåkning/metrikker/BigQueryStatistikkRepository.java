package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import org.hibernate.query.NativeQuery;

import no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker.bigquery.tabell.ProsessTaskFeilRecord;

@Dependent
public class BigQueryStatistikkRepository {

    private static final String UDEFINERT = "-";

    private EntityManager entityManager;

    BigQueryStatistikkRepository() {
        // for CDI proxy
    }

    @Inject
    public BigQueryStatistikkRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<ProsessTaskFeilRecord> hentAlle() {
        return prosessTaskFeilStatistikk();
    }

    @SuppressWarnings("unchecked")
    List<ProsessTaskFeilRecord> prosessTaskFeilStatistikk() {
        String sql =
            "select coalesce(f.ytelse_type, 'NONE'), f.saksnummer, p.id, p.task_type, p.status, p.siste_kjoering_slutt_ts, p.task_parametere"
                + " , p.blokkert_av, p.opprettet_tid, fpt.gruppe_sekvensnr"
                + " from prosess_task p " +
                " left outer join fagsak_prosess_task fpt ON fpt.prosess_task_id = p.id" +
                " left outer join fagsak f on f.id=fpt.fagsak_id" +
                " where ("
                + "       (p.status IN ('FEILET') AND p.siste_kjoering_feil_tekst IS NOT NULL)" // har feilet
                + "    OR (p.status IN ('KLAR', 'VETO') AND p.opprettet_tid < :ts AND (p.neste_kjoering_etter IS NULL OR p.neste_kjoering_etter < :ts2))"
                // har ligget med veto, klar lenge
                + "    OR (p.status IN ('VENTER_SVAR', 'SUSPENDERT') AND p.opprettet_tid < :ts )" // har ligget og ventet svar lenge
                + " )";

        LocalDateTime nå = LocalDateTime.now();

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("ts", nå.truncatedTo(ChronoUnit.DAYS))
            .setParameter("ts2", nå);

        Stream<Tuple> stream = query.getResultStream();

        List<ProsessTaskFeilRecord> values = stream.map(t -> {
                String ytelseType = t.get(0, String.class);
                String saksnummer = t.get(1, String.class);
                String taskId = t.get(2, Long.class).toString();
                String taskType = t.get(3, String.class);
                String status = t.get(4, String.class);
                var sistKjørt = t.get(5, LocalDateTime.class);
                String sistKjørtTid = sistKjørt == null ? null : sistKjørt.toInstant(ZoneOffset.UTC).toString();

                Long blokkertAvId = t.get(7, Long.class);
                String blokkertAv = blokkertAvId == null ? null : blokkertAvId.toString();

                String opprettetTid = t.get(8, LocalDateTime.class).toInstant(ZoneOffset.UTC).toString();

                var gruppeSekvensnr = t.get(9, Long.class);

                return new ProsessTaskFeilRecord(
                    coalesce(ytelseType, UDEFINERT),
                    coalesce(saksnummer, UDEFINERT),
                    taskId,
                    taskType,
                    status,
                    sistKjørtTid,
                    blokkertAv,
                    opprettetTid,
                    gruppeSekvensnr
                );
            })
            .toList();

        return values;
    }

    private static String coalesce(String str, String defValue) {
        return str != null ? str : defValue;
    }
}
