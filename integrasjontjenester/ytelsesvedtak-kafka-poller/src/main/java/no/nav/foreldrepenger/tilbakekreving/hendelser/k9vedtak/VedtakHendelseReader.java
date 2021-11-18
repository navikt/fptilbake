package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties.HENVISNING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.HendelseReader;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.K9HenvisningKonverterer;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@K9tilbake
public class VedtakHendelseReader implements HendelseReader {

    private static final Logger logger = LoggerFactory.getLogger(VedtakHendelseReader.class);
    public static final LocalDateTime BESTEMT_VEDTAK_DATO = LocalDateTime.of(LocalDate.of(2020, 10, 12), LocalTime.MIDNIGHT);

    private VedtakHendelseMeldingConsumer meldingConsumer;
    private ProsessTaskTjeneste taskTjeneste;

    VedtakHendelseReader() {
        // CDI
    }

    @Inject
    public VedtakHendelseReader(VedtakHendelseMeldingConsumer meldingConsumer,
                                ProsessTaskTjeneste taskTjeneste) {
        this.meldingConsumer = meldingConsumer;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public PostTransactionHandler hentOgBehandleMeldinger() {
        List<VedtakHendelse> meldinger = meldingConsumer.lesMeldinger();
        if (meldinger.isEmpty()) {
            return () -> {
            }; //trenger ikke å gjøre commit, siden ingen nye meldinger er lest
        }

        logger.info("Leste {} meldinger fra privat-k9-vedtakhendelse-topic", meldinger.size());
        behandleMeldinger(meldinger);
        return this::commitMeldinger;
    }

    private void behandleMeldinger(List<VedtakHendelse> meldinger) {
        for (VedtakHendelse melding : meldinger) {
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

    private void prosesserMelding(VedtakHendelse melding) {
        lagHåndterHendelseProsessTask(melding);
    }

    private void lagHåndterHendelseProsessTask(VedtakHendelse melding) {
        Set<FagsakYtelseType> støttedeFagsakYtelseTyper = Set.of(FagsakYtelseType.FRISINN, FagsakYtelseType.OMSORGSPENGER, FagsakYtelseType.PLEIEPENGER_SYKT_BARN);
        validereMelding(melding);
        if (støttedeFagsakYtelseTyper.contains(melding.getFagsakYtelseType())) {
            taskTjeneste.lagre(lagProsessTaskData(melding));
        } else {
            logger.info("Melding om vedtak for {} for behandling={} for {} med vedtakstidspunkt {} ble ignorert pga ikke-støttet ytelsetype",
                melding.getFagsakYtelseType(), melding.getBehandlingId(), melding.getFagsakYtelseType(), melding.getVedtattTidspunkt());
        }
    }

    private void validereMelding(VedtakHendelse melding) {
        Objects.requireNonNull(melding.getAktør().getId());
        Objects.requireNonNull(melding.getBehandlingId());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getFagsakYtelseType());
        Objects.requireNonNull(melding.getVedtattTidspunkt());
    }

    private ProsessTaskData lagProsessTaskData(VedtakHendelse melding) {
        Henvisning henvisning = K9HenvisningKonverterer.uuidTilHenvisning(melding.getBehandlingId());
        ProsessTaskData td = ProsessTaskData.forProsessTask(HåndterHendelseTask.class);
        td.setAktørId(melding.getAktør().getId());
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getBehandlingId().toString());
        td.setProperty(HENVISNING, henvisning.getVerdi());
        td.setSaksnummer(melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType().getKode());
        td.setNesteKjøringEtter(LocalDateTime.now().plusMinutes(10));
        return td;
    }

}
