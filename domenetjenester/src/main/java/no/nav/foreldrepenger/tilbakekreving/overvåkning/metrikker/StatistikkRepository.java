package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import org.hibernate.query.NativeQuery;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagOmrådeKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.sensu.SensuEvent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

@Dependent
public class StatistikkRepository {

    private static final Logger log = LoggerFactory.getLogger(StatistikkRepository.class);

    private static final String UDEFINERT = "-";

    static final List<String> PROSESS_TASK_STATUSER = Stream.of(ProsessTaskStatus.KLAR, ProsessTaskStatus.FEILET, ProsessTaskStatus.VENTER_SVAR, ProsessTaskStatus.SUSPENDERT).map(ProsessTaskStatus::getDbKode).toList();
    static final List<String> AKSJONSPUNKTER = AksjonspunktDefinisjon.kodeMap().values().stream()
        .filter(p -> !AksjonspunktDefinisjon.UNDEFINED.equals(p)).map(AksjonspunktDefinisjon::getKode).toList();
    static final List<String> AKSJONSPUNKT_STATUSER = AksjonspunktStatus.kodeMap().values().stream()
        .filter(p -> !AksjonspunktStatus.AVBRUTT.equals(p)).map(AksjonspunktStatus::getKode)
        .toList();

    static final List<String> BEHANDLING_RESULTAT_TYPER = List.copyOf(BehandlingResultatType.kodeMap().keySet());
    static final List<String> BEHANDLING_STATUS = List.copyOf(BehandlingStatus.kodeMap().keySet());
    static final List<String> FAGSAK_STATUS = List.copyOf(FagsakStatus.kodeMap().keySet());
    static final List<String> BEHANDLING_TYPER = BehandlingType.kodeMap().values().stream().filter(p -> !BehandlingType.UDEFINERT.equals(p)).map(BehandlingType::getKode).toList();

    private static final ObjectMapper OM = new ObjectMapper();

    static final String PROSESS_TASK_VER = "v1";
    private final Set<String> taskTyper;
    private final List<String> ytelseTypeKoder;
    private final Period kravgrunnlagOppdateringsperiode;

    private EntityManager entityManager;

    @Inject
    public StatistikkRepository(EntityManager entityManager,
                                @Any Instance<ProsessTaskHandler> handlers,
                                @KonfigVerdi(value = "metrikker.kravgrunnlag.oppdateringsperiode", defaultVerdi = "P1Y") Period kravgrunnlagOppdateringsperiode) {
        this.entityManager = entityManager;
        this.taskTyper = handlers.stream()
            .map(this::extractClass)
            .map(it -> it.getAnnotation(ProsessTask.class).value())
            .collect(Collectors.toSet());

        Fagsystem fagsystem = ApplicationName.hvilkenTilbake();
        Set<FagsakYtelseType> ytelsetyper = switch (fagsystem) {
            case FPTILBAKE -> Set.of(
                FagsakYtelseType.FORELDREPENGER,
                FagsakYtelseType.SVANGERSKAPSPENGER,
                FagsakYtelseType.ENGANGSTØNAD);
            case K9TILBAKE -> Set.of(
                FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE,
                FagsakYtelseType.OPPLÆRINGSPENGER,
                FagsakYtelseType.OMSORGSPENGER,
                FagsakYtelseType.FRISINN);
            default -> throw new IllegalArgumentException("Ikke-støttet applikasjon: " + fagsystem);
        };
        ytelseTypeKoder = ytelsetyper.stream().map(FagsakYtelseType::getKode).toList();
        this.kravgrunnlagOppdateringsperiode = kravgrunnlagOppdateringsperiode;
    }

    private Class<?> extractClass(ProsessTaskHandler bean) {
        if (!bean.getClass().isAnnotationPresent(ProsessTask.class) && bean instanceof TargetInstanceProxy<?> tip) {
            return tip.weld_getTargetInstance().getClass();
        } else {
            return bean.getClass();
        }
    }

    public List<SensuEvent> hentAlle() {
        LocalDate dag = LocalDate.now();

        List<SensuEvent> metrikker = new ArrayList<>();

        //følgende er kopiert fra k9-sak
        metrikker.addAll(timeCall(this::fagsakStatusStatistikk, "fagsakStatusStatistikk"));
        metrikker.addAll(timeCall(this::behandlingStatusStatistikk, "behandlingStatusStatistikk"));
        metrikker.addAll(timeCall(this::prosessTaskStatistikk, "prosessTaskStatistikk"));
        metrikker.addAll(timeCall(this::aksjonspunktStatistikk, "aksjonspunktStatistikk"));
        metrikker.addAll(timeCall(() -> aksjonspunktStatistikkDaglig(dag), "aksjonspunktStatistikkDaglig"));
        metrikker.addAll(timeCall(this::prosessTaskFeilStatistikk, "prosessTaskFeilStatistikk"));


        //tilpasset for k9/fp-tilbake
        metrikker.addAll(timeCall(this::meldingerFraØkonomiStatistikk, "meldingerFraØknomiStatistikk"));
        metrikker.addAll(timeCall(this::behandlingerOpprettet, "behandlingerOpprettet"));
        metrikker.addAll(timeCall(this::behandlingVedtak, "behandlingVedtak"));
        metrikker.addAll(timeCall(this::brevsporing, "brevsporing"));

        return metrikker;
    }

    private Collection<SensuEvent> timeCall(Supplier<Collection<SensuEvent>> collectionSupplier, String function) {
        log.info("Starter {}", function);
        var start = System.currentTimeMillis();
        var sensuEvents = collectionSupplier.get();
        var slutt = System.currentTimeMillis();

        log.info("{} benyttet {} ms", function, (slutt - start));

        return sensuEvents;
    }

    @SuppressWarnings("unchecked")
    Collection<SensuEvent> fagsakStatusStatistikk() {
        String sql = "select f.ytelse_type, f.fagsak_status, count(*) as antall" +
            "         from fagsak f" +
            "         group by f.ytelse_type, f.fagsak_status" +
            "         order by 1, 2";

        String metricField = "totalt_antall";
        String metricName = "fagsak_status_v1";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
            toMap(
                "ytelse_type", t.get(0, String.class),
                "fagsak_status", t.get(1, String.class)),
            Map.of("totalt_antall", t.get(2, BigDecimal.class)))).collect(Collectors.toCollection(LinkedHashSet::new));

        /* siden fagsak endrer status må vi ta hensyn til at noen verdier vil gå til 0, ellers vises siste verdi i stedet. */
        var zeroValues = emptyEvents(metricName,
            Map.of(
                "ytelse_type", ytelseTypeKoder,
                "fagsak_status", FAGSAK_STATUS),
            Map.of(
                metricField, BigDecimal.ZERO));

        values.addAll(zeroValues); // NB: utnytter at Set#addAll ikke legger til verdier som ikke finnes fra før

        return values;

    }

    @SuppressWarnings("unchecked")
    Collection<SensuEvent> behandlingStatusStatistikk() {
        String sql = "select f.ytelse_type, b.behandling_type, b.behandling_status, count(*) as antall" +
            "      from fagsak f" +
            "      inner join behandling b on b.fagsak_id=f.id" +
            "      group by f.ytelse_type, b.behandling_type, b.behandling_status" +
            "      order by 1, 2, 3";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        Stream<Tuple> stream = query.getResultStream();

        String metricName = "behandling_status_v1";
        String metricField = "totalt_antall";

        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                toMap(
                    "ytelse_type", t.get(0, String.class),
                    "behandling_type", t.get(1, String.class),
                    "behandling_status", t.get(2, String.class)),
                Map.of(
                    metricField, t.get(3, BigDecimal.class))))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        /* siden behandling endrer status må vi ta hensyn til at noen verdier vil gå til 0, ellers vises siste verdi i stedet. */
        var zeroValues = emptyEvents(metricName,
            Map.of(
                "ytelse_type", ytelseTypeKoder,
                "behandling_type", BEHANDLING_TYPER,
                "behandling_status", BEHANDLING_STATUS),
            Map.of(
                metricField, BigDecimal.ZERO));

        values.addAll(zeroValues); // NB: utnytter at Set#addAll ikke legger til verdier som ikke finnes fra før

        return values;

    }

    @SuppressWarnings("unchecked")
    Collection<SensuEvent> aksjonspunktStatistikk() {
        String sql = "select f.ytelse_type, a.aksjonspunkt_def as aksjonspunkt, a.aksjonspunkt_status," +
            " count(*) as antall" +
            " from aksjonspunkt a " +
            " inner join behandling b on b.id =a.behandling_id" +
            " inner join fagsak f on f.id=b.fagsak_id" +
            " where a.aksjonspunkt_status IN (:statuser)" +
            " group by f.ytelse_type, a.aksjonspunkt_def, a.aksjonspunkt_status";

        String metricName = "aksjonspunkt_per_ytelse_type_v1";
        String metricField = "totalt_antall";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("statuser", AKSJONSPUNKT_STATUSER.stream().collect(Collectors.toSet()));

        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                toMap(
                    "ytelse_type", t.get(0, String.class),
                    "aksjonspunkt", t.get(1, String.class),
                    "aksjonspunkt_status", t.get(2, String.class)),
                Map.of(
                    metricField, t.get(3, BigDecimal.class))))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        /* siden aksjonspunkt endrer status må vi ta hensyn til at noen verdier vil gå til 0, ellers vises siste verdi i stedet. */
        var zeroValues = emptyEvents(metricName,
            Map.of(
                "ytelse_type", ytelseTypeKoder,
                "aksjonspunkt", AKSJONSPUNKTER,
                "aksjonspunkt_status", AKSJONSPUNKT_STATUSER),
            Map.of(
                metricField, BigDecimal.ZERO));

        values.addAll(zeroValues); // NB: utnytter at Set#addAll ikke legger til verdier som ikke finnes fra før

        return values;

    }

    @SuppressWarnings("unchecked")
    Collection<SensuEvent> aksjonspunktStatistikkDaglig(LocalDate dato) {
        String sql = "select f.ytelse_type, f.saksnummer, b.id as behandling_id, a.aksjonspunkt_def as aksjonspunkt, " +
            "      a.aksjonspunkt_status as status,a.vent_aarsak, coalesce(a.endret_tid, a.opprettet_tid) as tid" +
            " from aksjonspunkt a " +
            " inner join behandling b on b.id =a.behandling_id" +
            " inner join fagsak f on f.id=b.fagsak_id" +
            " where a.aksjonspunkt_status IN (:statuser)"
            + " and coalesce(a.endret_tid, a.opprettet_tid)>=:startAvDag and coalesce(a.endret_tid, a.opprettet_tid) < :nesteDag";

        String metricName = "aksjonspunkt_daglig_v2";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("statuser", AKSJONSPUNKT_STATUSER.stream().collect(Collectors.toSet()))
            .setParameter("startAvDag", dato.atStartOfDay())
            .setParameter("nesteDag", dato.plusDays(1).atStartOfDay());

        Stream<Tuple> stream = query.getResultStream();

        var values = stream.map(t -> {
                String ytelseType = t.get(0, String.class);
                String saksnummer = t.get(1, String.class);
                String behandlingId = t.get(2, BigDecimal.class).toString();
                String aksjonspunktKode = t.get(3, String.class);
                String aksjonspunktNavn = coalesce(AksjonspunktDefinisjon.kodeMap().getOrDefault(aksjonspunktKode, AksjonspunktDefinisjon.UNDEFINED).getNavn(), UDEFINERT);
                String aksjonspunktStatus = t.get(4, String.class);
                String venteÅrsak = coalesce(t.get(5, String.class), UDEFINERT);
                long tidsstempel = t.get(6, Timestamp.class).getTime();
                return SensuEvent.createSensuEvent(metricName,
                    toMap(
                        "ytelse_type", ytelseType,
                        "aksjonspunkt_status", aksjonspunktStatus,
                        "vente_arsak", venteÅrsak,
                        "aksjonspunkt", aksjonspunktKode),
                    Map.of(
                        "aksjonspunkt_navn", aksjonspunktNavn,
                        "saksnummer", saksnummer,
                        "behandlingId", behandlingId),
                    tidsstempel);
            })
            .toList();

        return values;

    }


    @SuppressWarnings("unchecked")
    Collection<SensuEvent> prosessTaskStatistikk() {

        // hardkoder statuser for bedre access plan for partisjon i db
        String sql = " select coalesce(f.ytelse_type, 'NONE') as ytelse_type, p.task_type, p.status, count(*) antall " +
            " from prosess_task p " +
            " left outer join fagsak_prosess_task fpt on fpt.prosess_task_id=p.id" +
            " left outer join fagsak f on f.id=fpt.fagsak_id" +
            " where p.status IN (:statuser)" +
            " group by coalesce(f.ytelse_type, 'NONE'), p.task_type, p.status " +
            " order by 1, 2, 3";

        String metricName = "prosess_task_" + PROSESS_TASK_VER;
        String metricField = "totalt_antall";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("statuser", PROSESS_TASK_STATUSER);

        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                toMap(
                    "ytelse_type", t.get(0, String.class),
                    "prosess_task_type", t.get(1, String.class),
                    "status", t.get(2, String.class)),
                Map.of(
                    metricField, t.get(3, BigDecimal.class))))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        /* siden aksjonspunkt endrer status må vi ta hensyn til at noen verdier vil gå til 0, ellers vises siste verdi i stedet. */
        var zeroValues = emptyEvents(metricName,
            Map.of(
                "ytelse_type", ytelseTypeKoder,
                "prosess_task_type", taskTyper,
                "status", PROSESS_TASK_STATUSER),
            Map.of(
                metricField, BigDecimal.ZERO));

        values.addAll(zeroValues); // NB: utnytter at Set#addAll ikke legger til verdier som ikke finnes fra før

        return values;
    }

    Collection<SensuEvent> prosessTaskFeilStatistikk() {
        String sql = "select coalesce(f.ytelse_type, 'NONE'), f.saksnummer, p.id, p.task_type, p.status, p.siste_kjoering_slutt_ts, p.siste_kjoering_feil_tekst, p.task_parametere"
            + " , p.blokkert_av, p.opprettet_tid, fpt.gruppe_sekvensnr"
            + " from prosess_task p " +
            " left outer join fagsak_prosess_task fpt ON fpt.prosess_task_id = p.id" +
            " left outer join fagsak f on f.id=fpt.fagsak_id" +
            " where ("
            + "       (p.status IN ('FEILET') AND p.siste_kjoering_feil_tekst IS NOT NULL)" // har feilet
            + "    OR (p.status IN ('KLAR', 'VETO') AND p.opprettet_tid < :ts AND (p.neste_kjoering_etter IS NULL OR p.neste_kjoering_etter < :ts2))" // har ligget med veto, klar lenge
            + "    OR (p.status IN ('VENTER_SVAR', 'SUSPENDERT') AND p.opprettet_tid < :ts )" // har ligget og ventet svar lenge
            + " )";

        String metricName = "prosess_task_feil_log_" + PROSESS_TASK_VER;
        LocalDateTime nå = LocalDateTime.now();

        @SuppressWarnings("unchecked")
        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("ts", nå.truncatedTo(ChronoUnit.DAYS))
            .setParameter("ts2", nå);

        Stream<Tuple> stream = query.getResultStream();

        long now = System.currentTimeMillis();

        Collection<SensuEvent> values = stream.map(t -> {
                String ytelseType = t.get(0, String.class);
                String saksnummer = t.get(1, String.class);
                String taskId = t.get(2, BigDecimal.class).toString();
                String taskType = t.get(3, String.class);
                String status = t.get(4, String.class);
                Timestamp sistKjørt = t.get(5, Timestamp.class);
                long tidsstempel = sistKjørt == null ? now : sistKjørt.getTime();

                String sisteFeil = finnStacktraceStartFra(t.get(6, String.class), 500).orElse(UDEFINERT);
                String taskParams = t.get(7, String.class);

                BigDecimal blokkertAvId = t.get(8, BigDecimal.class);
                String blokkertAv = blokkertAvId == null ? null : blokkertAvId.toString();

                String opprettetTid = t.get(9, Timestamp.class).toInstant().toString();

                var gruppeSekvensnr = t.get(10, BigDecimal.class);

                return SensuEvent.createSensuEvent(metricName,
                    toMap(
                        "ytelseType", coalesce(ytelseType, UDEFINERT),
                        "status", status,
                        "prosess_task_type", taskType),
                    Map.of(
                        "taskId", taskId,
                        "saksnummer", coalesce(saksnummer, UDEFINERT),
                        "siste_feil", sisteFeil,
                        "task_parametere", coalesce(taskParams, UDEFINERT),
                        "blokkert_av", coalesce(blokkertAv, UDEFINERT),
                        "opprettet_tid", opprettetTid,
                        "gruppe_sekvensnr", gruppeSekvensnr == null ? UDEFINERT : gruppeSekvensnr.toString()),
                    tidsstempel);
            })
            .toList();

        return values;
    }


    Collection<SensuEvent> meldingerFraØkonomiStatistikk() {

        LocalDateTime startpunkt = LocalDateTime.now().minus(kravgrunnlagOppdateringsperiode);
        Date startpunktDbTid = new Date(startpunkt.toEpochSecond(ZoneOffset.UTC));
        String sql = """
            select tidspunkt, meldingstype, fagomraade, count(*) as antall
            from (
              select opprettet_tid as tidspunkt,
              case
                when melding like '<?xml version="1.0" encoding="utf-8"?><urn:detaljertKravgrunnlagMelding%' then 'KRAVGRUNNLAG' 
                when melding like '<?xml version="1.0" encoding="utf-8"?><urn:endringKravOgVedtakstatus%' then 'STATUSMELDING'
                else 'UKJENT' end as meldingstype,
                cast(regexp_substr(melding, 'kodeStatusKrav>([^<]*)<', 1, 1, null, 1) as varchar2(10 char)) as status,
                cast(regexp_substr(melding, 'kodeFagomraade>([^<]*)<', 1, 1, null, 1) as varchar2(10 char)) as fagomraade
              from OKO_XML_MOTTATT
              where opprettet_tid > :starttid
            )
            group by tidspunkt, meldingstype, status, fagomraade
            """;

        String metricName = "meldinger_fra_OS_v1";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("starttid", startpunktDbTid);
        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                Map.of("meldingstype", t.get(1, String.class),
                    "status", t.get(2, String.class),
                    "fagomraade", t.get(3, String.class),
                    "ytelseType", mapFagområdeTilYtelseType(t.get(3, String.class)).getKode()
                ),
                Map.of("totalt_antall", t.get(4, BigDecimal.class)),
                t.get(0, Timestamp.class).getTime()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return values;
    }

    private static FagsakYtelseType mapFagområdeTilYtelseType(String fagområdeKode) {
        FagOmrådeKode fagOmrådeKode = FagOmrådeKode.fraKode(fagområdeKode);
        return switch (fagOmrådeKode) {
            case FORELDREPENGER -> FagsakYtelseType.FORELDREPENGER;
            case ENGANGSSTØNAD -> FagsakYtelseType.ENGANGSTØNAD;
            case SVANGERSKAPSPENGER -> FagsakYtelseType.SVANGERSKAPSPENGER;
            case PLEIEPENGER_SYKT_BARN -> FagsakYtelseType.PLEIEPENGER_SYKT_BARN;
            case PLEIEPENGER_NÆRSTÅENDE -> FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE;
            case OMSORGSPENGER -> FagsakYtelseType.OMSORGSPENGER;
            case OPPLÆRINGSPENGER -> FagsakYtelseType.OPPLÆRINGSPENGER;
            case FRISINN -> FagsakYtelseType.FRISINN;
            default -> FagsakYtelseType.UDEFINERT; //alt som ikke er forventet, mappes til udefinert, så kan man evt. overvåke det i grafana
        };
    }

    Collection<SensuEvent> behandlingerOpprettet() {
        String sql = """
            select time, behandling_type, behandling_status, ytelse_type, opprettelsegrunn, count(*) as antall
            from (
              SELECT B.OPPRETTET_TID AS TIME,
                   B.BEHANDLING_TYPE,
                   B.BEHANDLING_STATUS,
                   F.YTELSE_TYPE,
                   oxm.opprettet_tid as kravgrunnlag_mottatt_tid,
                   rank() over (partition by B.ID order by oxm.opprettet_tid asc) rnk,
                   case when manuelt_opprettet = 'N' then (case when b.opprettet_tid > oxm.opprettet_tid + interval '7' day then 'AUTOMATISK_FRA_GAMMELT_KRAVGRUNNLAG' else 'AUTOMATISK_FRA_YTELSEBEHANDLING' end) else 'MANUELT_OPPRETTET' end as opprettelsegrunn
              FROM BEHANDLING B
              INNER JOIN FAGSAK F ON B.FAGSAK_ID = F.ID
              LEFT OUTER JOIN EKSTERN_BEHANDLING eb on eb.intern_id = b.id
              LEFT OUTER JOIN OKO_XML_MOTTATT oxm on oxm.henvisning = eb.henvisning and oxm.melding like '<?xml version="1.0" encoding="utf-8"?><urn:detaljertKravgrunnlagMelding%'
            ) where rnk = 1
            group by time, behandling_type, behandling_status, ytelse_type, opprettelsegrunn
            order by 1, 2, 3, 4, 5
            """;

        String metricName = "behandlinger_opprettet_v1";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                Map.of("meldingstype", t.get(1, String.class)),
                Map.of("totalt_antall", t.get(2, BigDecimal.class)),
                t.get(0, Timestamp.class).getTime()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return values;
    }

    Collection<SensuEvent> behandlingVedtak() {
        String sql = """
            SELECT B.AVSLUTTET_DATO, B.BEHANDLING_TYPE, BR.BEHANDLING_RESULTAT_TYPE, B.SAKSBEHANDLING_TYPE, YTELSE_TYPE, COUNT(*) AS ANTALL
            FROM BEHANDLING B
            INNER JOIN BEHANDLING_RESULTAT BR ON B.ID = BR.BEHANDLING_ID
            INNER JOIN FAGSAK F ON B.FAGSAK_ID = F.ID
            WHERE B.AVSLUTTET_DATO IS NOT NULL
            GROUP BY B.AVSLUTTET_DATO,
                     B.BEHANDLING_TYPE,
                     BR.BEHANDLING_RESULTAT_TYPE,
                     B.SAKSBEHANDLING_TYPE,
                     F.YTELSE_TYPE
            """;

        String metricName = "behandling_vedtak_v1";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                Map.of("behandlingtype", t.get(1, String.class),
                    "behandlingResultatType", t.get(2, String.class),
                    "saksbehandlingType", t.get(3, String.class),
                    "ytelseType", t.get(4, String.class)
                ),
                Map.of("totalt_antall", t.get(2, BigDecimal.class)),
                t.get(0, Timestamp.class).getTime()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return values;
    }

    Collection<SensuEvent> brevsporing() {
        String sql = """
            SELECT BS.OPPRETTET_TID, BS.BREV_TYPE, F.YTELSE_TYPE, COUNT(BS.ID) AS ANTALL
            FROM BREV_SPORING BS
            JOIN BEHANDLING B ON BS.BEHANDLING_ID = B.ID
            JOIN FAGSAK F ON B.FAGSAK_ID = F.ID       
            GROUP BY BS.OPPRETTET_TID,
                     BS.BREV_TYPE,
                     F.YTELSE_TYPE
            """;

        String metricName = "brev_v1";

        NativeQuery<Tuple> query = (NativeQuery<Tuple>) entityManager.createNativeQuery(sql, Tuple.class);
        Stream<Tuple> stream = query.getResultStream();
        var values = stream.map(t -> SensuEvent.createSensuEvent(metricName,
                Map.of("brev_type", t.get(1, String.class),
                    "ytelseType", t.get(2, String.class)
                ),
                Map.of("totalt_antall", t.get(3, BigDecimal.class)),
                t.get(0, Timestamp.class).getTime()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return values;
    }

    private static String coalesce(String str, String defValue) {
        return str != null ? str : defValue;
    }

    private static Optional<String> finnStacktraceStartFra(String sisteFeil, int maksLen) {
        boolean guessItsJson = sisteFeil != null && sisteFeil.startsWith("{");
        if (guessItsJson) {
            try {
                var feil = OM.readValue(sisteFeil, ProsessTaskFeil.class);
                var strFeil = feil.getStackTrace();
                return strFeil == null ? Optional.empty() : Optional.of(strFeil.substring(0, Math.min(maksLen, strFeil.length()))); // chop-chop
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Ugyldig json: " + sisteFeil, e);
            }
        }
        return Optional.empty();
    }

    /**
     * Lager events med 0 målinger for alle kombinasjoner av oppgitte vektorer.
     */
    private Collection<SensuEvent> emptyEvents(String metricName, Map<String, Collection<String>> vectors, Map<String, Object> defaultVals) {
        List<Map<String, String>> matrix = new CombineLists<String>(vectors).toMap();
        return matrix.stream()
            .map(v -> SensuEvent.createSensuEvent(metricName, v, defaultVals))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Map.of() takler ikke null verdier, så vi lager vår egen variant.
     */
    private static Map<String, String> toMap(String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Må ha partall antall argumenter, fikk: " + Arrays.asList(args));
        }
        var map = new HashMap<String, String>();
        for (int i = 0; i < args.length; i += 2) {
            // influxdb Point takler ikke null key eller value. skipper null verdier
            String v = args[i + 1];
            if (v != null) {
                map.put(args[i], v);
            }
        }
        return map;
    }
}
