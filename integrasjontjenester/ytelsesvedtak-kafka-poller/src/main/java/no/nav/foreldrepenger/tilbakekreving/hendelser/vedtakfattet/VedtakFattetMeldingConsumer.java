package no.nav.foreldrepenger.tilbakekreving.hendelser.vedtakfattet;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.YtelsesvedtakHendelseConsumer;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.JsonDeserialiserer;

@ApplicationScoped
public class VedtakFattetMeldingConsumer extends YtelsesvedtakHendelseConsumer {

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    VedtakFattetMeldingConsumer() {
        // CDI
    }

    @Inject
    public VedtakFattetMeldingConsumer(@KonfigVerdi(VEDTAKFATTET_TOPIC) String topic,
                                       @KonfigVerdi(BOOTSTRAP_SERVERS) String bootstrapServers,
                                       @KonfigVerdi("systembruker.username") String username,
                                       @KonfigVerdi("systembruker.password") String password) {
        Properties properties = lagFellesProperty(bootstrapServers, ApplicationName.hvilkenTilbakeAppName());
        this.setSecurity(username, properties);
        this.addUserToProperties(username, password, properties);
        this.kafkaConsumer = new KafkaConsumer<>(properties);
        this.topic = topic;
        this.subscribe();
    }

    @Inject
    public List<Ytelse> lesMeldinger() {
        List<Ytelse> meldinger = new ArrayList<>();
        ConsumerRecords<String, String> records = this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        for (ConsumerRecord<String, String> record : records) {
            meldinger.add(JsonDeserialiserer.deserialiser(record.value(), Ytelse.class));
        }
        return meldinger;
    }

    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    public List<Ytelse> lesMeldingerHeltFraStarten() {
        this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        this.kafkaConsumer.seekToBeginning(this.kafkaConsumer.assignment());
        return lesMeldinger();
    }

    private void subscribe() {
        this.kafkaConsumer.subscribe(Collections.singletonList(this.topic));
    }

    public String getTopic() {
        return topic;
    }
}
