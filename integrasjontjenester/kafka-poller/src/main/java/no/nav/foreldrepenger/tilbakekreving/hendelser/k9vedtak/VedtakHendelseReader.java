package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.HENVISNING;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.K9HenvisningKonverterer;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class VedtakHendelseReader {

    private static final Logger logger = LoggerFactory.getLogger(VedtakHendelseReader.class);

    private VedtakHendelseMeldingConsumer meldingConsumer;
    private ProsessTaskRepository prosessTaskRepository;

    VedtakHendelseReader() {
        // CDI
    }

    @Inject
    public VedtakHendelseReader(VedtakHendelseMeldingConsumer meldingConsumer,
                                ProsessTaskRepository prosessTaskRepository) {
        this.meldingConsumer = meldingConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
    }

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
            throw KafkaConsumerFeil.FACTORY.kunneIkkeCommitOffset(e).toException();
        }
    }

    private void prosesserMelding(VedtakHendelse melding) {
        lagHåndterHendelseProsessTask(melding);
    }

    private void lagHåndterHendelseProsessTask(VedtakHendelse melding) {
        validereMelding(melding);
        prosessTaskRepository.lagre(lagProsessTaskData(melding));
    }

    private void validereMelding(VedtakHendelse melding) {
        Objects.requireNonNull(melding.getAktør().getId());
        Objects.requireNonNull(melding.getBehandlingId());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getFagsakYtelseType());
    }

    private ProsessTaskData lagProsessTaskData(VedtakHendelse melding){
        Henvisning henvisning = K9HenvisningKonverterer.uuidTilHenvisning(melding.getBehandlingId());
        ProsessTaskData td = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);
        td.setAktørId(melding.getAktør().getId());
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getBehandlingId().toString());
        td.setProperty(HENVISNING, henvisning.getVerdi());
        td.setSaksnummer(melding.getSaksnummer());
        td.setProperty(FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType().getKode());
        return td;
    }

}
