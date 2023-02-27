package no.nav.foreldrepenger.tilbakekreving.hendelser;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.log.metrics.Controllable;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.felles.KafkaIntegration;
import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;

@ApplicationScoped
public class VedtakConsumer implements AppServiceHandler, Controllable, KafkaIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(VedtakConsumer.class);
    private KafkaStreams stream;
    private String topic;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(@KonfigVerdi(value = "kafka.fattevedtak.topic", defaultVerdi = "teamforeldrepenger.familie-vedtakfattet-v1") String topicName,
                          VedtaksHendelseHåndterer vedtaksHendelseHåndterer) {
        this.topic = topicName;
        var applicationId = ApplicationName.hvilkenTilbakeAppName() + "-vedtak"; // Hold denne konstant pga offset-commit

        final Consumed<String, String> consumed = Consumed.with(Topology.AutoOffsetReset.EARLIEST);

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic, consumed)
            .foreach(vedtaksHendelseHåndterer::handleMessage);

        this.stream = new KafkaStreams(builder.build(), KafkaProperties.forStreamsStringValue(applicationId));
    }

    private void addShutdownHooks() {
        stream.setStateListener((newState, oldState) -> {
            LOG.info("{} :: From state={} to state={}", topic, oldState, newState);

            if (newState == KafkaStreams.State.ERROR) {
                // if the stream has died there is no reason to keep spinning
                LOG.warn("{} :: No reason to keep living, closing stream", topic);
                stop();
            }
        });
        stream.setUncaughtExceptionHandler((t, e) -> {
            LOG.error(topic + " :: Caught exception in stream, exiting", e);
            stop();
        });
    }


    @Override
    public boolean isAlive() {
        return stream != null && stream.state().isRunningOrRebalancing();
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void start() {
        addShutdownHooks();
        stream.start();
        LOG.info("Starter konsumering av topic={}, tilstand={}", topic, stream.state());
    }

    @Override
    public void stop() {
        int timeoutSekunder = 30;
        LOG.info("Starter shutdown av topic={}, tilstand={} med {} sekunder timeout", topic, stream.state(), timeoutSekunder);
        stream.close(Duration.of(timeoutSekunder, ChronoUnit.SECONDS));
        LOG.info("Shutdown av topic={}, tilstand={} med {} sekunder timeout", topic, stream.state(), timeoutSekunder);
    }
}
