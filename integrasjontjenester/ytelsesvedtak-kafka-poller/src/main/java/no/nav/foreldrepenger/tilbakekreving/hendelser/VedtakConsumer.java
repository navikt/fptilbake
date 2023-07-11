package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;
import no.nav.vedtak.log.metrics.Controllable;
import no.nav.vedtak.log.metrics.LiveAndReadinessAware;

@ApplicationScoped
public class VedtakConsumer implements LiveAndReadinessAware, Controllable {

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

        final var builder = new StreamsBuilder();
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
        stream.setUncaughtExceptionHandler(ex -> {
            LOG.error(topic + " :: Caught exception in stream, exiting", ex);
            return SHUTDOWN_CLIENT;
        });
    }


    @Override
    public boolean isAlive() {
        return stream != null && stream.state().isRunningOrRebalancing();
    }

    @Override
    public boolean isReady() {
        return isAlive();
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
        var timeoutSekunder = 30;
        LOG.info("Starter shutdown av topic={}, tilstand={} med {} sekunder timeout", topic, stream.state(), timeoutSekunder);
        stream.close(Duration.of(timeoutSekunder, ChronoUnit.SECONDS));
        LOG.info("Shutdown av topic={}, tilstand={} med {} sekunder timeout", topic, stream.state(), timeoutSekunder);
    }
}
