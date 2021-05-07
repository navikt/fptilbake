package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.EKSTERN_BEHANDLING_ID;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.EKSTERN_BEHANDLING_UUID;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.FAGSAK_YTELSE_TYPE;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.HENVISNING;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties.SAKSNUMMER;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.HendelseReader;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@Fptilbake
public class TilkjentYtelseReader implements HendelseReader {

    private static final Logger logger = LoggerFactory.getLogger(TilkjentYtelseReader.class);

    private TilkjentYtelseMeldingConsumer meldingConsumer;
    private ProsessTaskRepository prosessTaskRepository;

    TilkjentYtelseReader() {
        // CDI
    }

    @Inject
    public TilkjentYtelseReader(TilkjentYtelseMeldingConsumer meldingConsumer,
                                ProsessTaskRepository prosessTaskRepository) {
        this.meldingConsumer = meldingConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public PostTransactionHandler hentOgBehandleMeldinger() {
        List<TilkjentYtelseMelding> meldinger = meldingConsumer.lesMeldinger();
        if (meldinger.isEmpty()) {
            return () -> {
            }; //trenger ikke å gjøre commit, siden ingen nye meldinger er lest
        }

        logger.info("Leste {} meldinger fra fp-tilkjentytelse-v1-topic", meldinger.size());
        behandleMeldinger(meldinger);
        return this::commitMeldinger;
    }

    private void behandleMeldinger(List<TilkjentYtelseMelding> meldinger) {
        for (TilkjentYtelseMelding melding : meldinger) {
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

    private void prosesserMelding(TilkjentYtelseMelding melding) {
        lagHåndterHendelseProsessTask(melding);
    }

    private void lagHåndterHendelseProsessTask(TilkjentYtelseMelding melding) {
        validereMelding(melding);
        prosessTaskRepository.lagre(lagProsessTaskData(melding));
    }

    private void validereMelding(TilkjentYtelseMelding melding) {
        Objects.requireNonNull(melding.getAktørId());
        Objects.requireNonNull(melding.getBehandlingId());
        Objects.requireNonNull(melding.getBehandlingUuid());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getFagsakYtelseType());
    }

    private ProsessTaskData lagProsessTaskData(TilkjentYtelseMelding melding){
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(melding.getBehandlingId());
        ProsessTaskData td = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);
        td.setAktørId(melding.getAktørId().getId());
        td.setProperty(EKSTERN_BEHANDLING_UUID, melding.getBehandlingUuid().toString());
        td.setProperty(EKSTERN_BEHANDLING_ID, henvisning.getVerdi()); //TODO k9-tilbake fjern når transisjon til henvisning er ferdig
        td.setProperty(HENVISNING, henvisning.getVerdi());
        td.setProperty(SAKSNUMMER, melding.getSaksnummer().getVerdi());
        td.setProperty(FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType());
        return td;
    }

}
