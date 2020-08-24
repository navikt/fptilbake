package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.KafkaConsumerFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TilkjentYtelseReader {

    private static final Logger logger = LoggerFactory.getLogger(TilkjentYtelseReader.class);

    private TilkjentYtelseMeldingConsumer meldingConsumer;
    private ProsessTaskRepository prosessTaskRepository;
    private String applikasjonNavn;

    TilkjentYtelseReader() {
        // CDI
    }

    @Inject
    public TilkjentYtelseReader(TilkjentYtelseMeldingConsumer meldingConsumer,
                                ProsessTaskRepository prosessTaskRepository,
                                @KonfigVerdi("application.name") String applikasjonNavn) {
        this.meldingConsumer = meldingConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
        this.applikasjonNavn = applikasjonNavn;
    }

    public PostTransactionHandler hentOgBehandleMeldinger() {
        // midlertidig kode, slettes når k9tilbake er klar til å lese meldinger fra k9-sak
        if(applikasjonNavn != null && applikasjonNavn.equals("k9-tilbake")){
            return () -> {
            };
        }
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
            throw KafkaConsumerFeil.FACTORY.kunneIkkeCommitOffset(e).toException();
        }
    }

    private void prosesserMelding(TilkjentYtelseMelding melding) {
        lagHåndterHendelseProsessTask(melding);
    }

    private void lagHåndterHendelseProsessTask(TilkjentYtelseMelding melding) {
        validereMelding(melding);
        HendelseTaskDataWrapper dataWrapper = HendelseTaskDataWrapper.lagWrapperForHendelseHåndtering(melding);
        prosessTaskRepository.lagre(dataWrapper.getProsessTaskData());
    }

    private void validereMelding(TilkjentYtelseMelding melding) {
        Objects.requireNonNull(melding.getAktørId());
        Objects.requireNonNull(melding.getBehandlingId());
        Objects.requireNonNull(melding.getBehandlingUuid());
        Objects.requireNonNull(melding.getSaksnummer());
        Objects.requireNonNull(melding.getFagsakYtelseType());
    }

}
