package no.nav.foreldrepenger.tilbakekreving.hendelser.vedtakfattet;

import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.FAGSAK_YTELSE_TYPE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterVedtakFattetTask;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class VedtakFattetReader {

    private static final Logger logger = LoggerFactory.getLogger(VedtakFattetReader.class);

    private static final Map<Fagsystem, Set<YtelseType>> STØTTET_YTELSE_TYPER = Map.of(
        Fagsystem.FPTILBAKE, Set.of(YtelseType.ENGANGSTØNAD, YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER),
        Fagsystem.K9TILBAKE,  Set.of(YtelseType.FRISINN, YtelseType.OMSORGSPENGER, YtelseType.PLEIEPENGER_SYKT_BARN)
    );

    private static final Map<YtelseType, FagsakYtelseType> YTELSE_TYPE_MAP = Map.of(
        YtelseType.ENGANGSTØNAD, FagsakYtelseType.ENGANGSTØNAD,
        YtelseType.FORELDREPENGER, FagsakYtelseType.FORELDREPENGER,
        YtelseType.SVANGERSKAPSPENGER, FagsakYtelseType.SVANGERSKAPSPENGER,
        YtelseType.FRISINN, FagsakYtelseType.FRISINN,
        YtelseType.OMSORGSPENGER, FagsakYtelseType.OMSORGSPENGER,
        YtelseType.OPPLÆRINGSPENGER, FagsakYtelseType.OPPLÆRINGSPENGER,
        YtelseType.PLEIEPENGER_SYKT_BARN, FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
        YtelseType.PLEIEPENGER_NÆRSTÅENDE, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE
    );

    private VedtakFattetMeldingConsumer meldingConsumer;
    private ProsessTaskTjeneste taskTjeneste;
    private Set<YtelseType> abonnerteYtelser;


    VedtakFattetReader() {
        // CDI
    }

    @Inject
    public VedtakFattetReader(VedtakFattetMeldingConsumer meldingConsumer,
                              ProsessTaskTjeneste taskTjeneste) {
        this.meldingConsumer = meldingConsumer;
        this.taskTjeneste = taskTjeneste;
        this.abonnerteYtelser = STØTTET_YTELSE_TYPER.getOrDefault(ApplicationName.hvilkenTilbake(), Set.of());

    }

    public VedtakFattetReader(VedtakFattetMeldingConsumer meldingConsumer,
                              ProsessTaskTjeneste taskTjeneste,
                              Fagsystem applikasjon) {
        this.meldingConsumer = meldingConsumer;
        this.taskTjeneste = taskTjeneste;
        this.abonnerteYtelser = STØTTET_YTELSE_TYPER.getOrDefault(applikasjon, Set.of());;
    }

    public PostTransactionHandler hentOgBehandleMeldinger() {
        List<Ytelse> meldinger = meldingConsumer.lesMeldinger();
        if (meldinger.isEmpty()) {
            return () -> {
            }; //trenger ikke å gjøre commit, siden ingen nye meldinger er lest
        }

        logger.info("Leste {} meldinger fra topic {}", meldinger.size(), meldingConsumer.getTopic());
        behandleMeldinger(meldinger);
        return this::commitMeldinger;
    }

    private void behandleMeldinger(List<Ytelse> meldinger) {
        for (Ytelse melding : meldinger) {
            prosesserMelding(melding);
        }
    }

    public void commitMeldinger() {
        try {
            meldingConsumer.manualCommitSync();
        } catch (CommitFailedException e) {
            throw KafkaConsumerFeil.kunneIkkeCommitOffset(e);
        }
    }

    private void prosesserMelding(Ytelse melding) {
        if (melding instanceof YtelseV1 ytelseV1) {
            lagHåndterHendelseProsessTask(ytelseV1);
        }
    }

    private void lagHåndterHendelseProsessTask(YtelseV1 melding) {

        validereMelding(melding);
        if (abonnerteYtelser.contains(melding.getType())) {
            taskTjeneste.lagre(lagProsessTaskData(melding));
        } else {
            logger.warn("Melding om vedtak for {} for sak={} behandling={} med vedtakstidspunkt {} ble ignorert pga ikke-støttet ytelsetype",
                melding.getType(), melding.getSaksnummer(), melding.getVedtakReferanse(), melding.getVedtattTidspunkt());
        }
    }

    private void validereMelding(YtelseV1 melding) {
        Objects.requireNonNull(melding.getAktør());
        Objects.requireNonNull(melding.getVedtakReferanse());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getType());
        Objects.requireNonNull(melding.getVedtattTidspunkt());
        Objects.requireNonNull(YTELSE_TYPE_MAP.get(melding.getType()));
    }

    private ProsessTaskData lagProsessTaskData(YtelseV1 melding) {
        ProsessTaskData td = ProsessTaskData.forProsessTask(HåndterVedtakFattetTask.class);
        td.setAktørId(melding.getAktør().getVerdi());
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getVedtakReferanse());
        td.setSaksnummer(melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE, YTELSE_TYPE_MAP.get(melding.getType()).getKode());
        // Begynner med å kjøre denne senere enn gamle topics - neste steg er å bytte på etter det er gjort catchup.
        td.setNesteKjøringEtter(LocalDateTime.now().plusMinutes(10));
        return td;
    }

}
