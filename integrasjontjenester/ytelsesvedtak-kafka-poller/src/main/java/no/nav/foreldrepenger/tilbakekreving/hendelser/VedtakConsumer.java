package no.nav.foreldrepenger.tilbakekreving.hendelser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.kafka.KafkaConsumerManager;
import no.nav.vedtak.server.Controllable;
import no.nav.vedtak.server.LiveAndReadinessAware;

@ApplicationScoped
public class VedtakConsumer implements LiveAndReadinessAware, Controllable {

    private static final Logger LOG = LoggerFactory.getLogger(VedtakConsumer.class);
    private KafkaConsumerManager<String, String> kcm;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(VedtaksHendelseHåndterer vedtaksHendelseHåndterer) {
        this.kcm = new KafkaConsumerManager<>(vedtaksHendelseHåndterer);
    }

    @Override
    public boolean isAlive() {
        return kcm.allRunning();
    }

    @Override
    public boolean isReady() {
        return isAlive();
    }

    @Override
    public void start() {
        LOG.info("Starter konsumering av topics={}", kcm.topicNames());
        kcm.start((t, e) -> LOG.error("{} :: Caught exception in stream, exiting", t, e));
    }

    @Override
    public void stop() {
        LOG.info("Starter shutdown av topics={} med 10 sekunder timeout", kcm.topicNames());
        kcm.stop();
    }
}
