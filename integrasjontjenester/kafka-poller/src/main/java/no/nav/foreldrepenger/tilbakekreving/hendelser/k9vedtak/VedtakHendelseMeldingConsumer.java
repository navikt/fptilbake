package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

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

import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.HendelseMeldingConsumer;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.JsonDeserialiserer;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class VedtakHendelseMeldingConsumer extends HendelseMeldingConsumer {

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    VedtakHendelseMeldingConsumer() {
        // CDI
    }

    @Inject
    public VedtakHendelseMeldingConsumer(@KonfigVerdi(FEED_URL) String topic,
                                         @KonfigVerdi(BOOTSTRAP_SERVERS) String bootstrapServers,
                                         @KonfigVerdi("application.name") String applikasjonNavn,
                                         @KonfigVerdi("systembruker.username") String username,
                                         @KonfigVerdi("systembruker.password") String password) {
        Properties properties = lagFellesProperty(bootstrapServers, applikasjonNavn);
        this.setSecurity(username, properties);
        this.addUserToProperties(username, password, properties);
        this.kafkaConsumer = new KafkaConsumer<>(properties);
        this.topic = topic;
        this.subscribe();
    }

    @Inject
    public List<VedtakHendelse> lesMeldinger() {
        List<VedtakHendelse> meldinger = new ArrayList<>();
        ConsumerRecords<String, String> records = this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        for (ConsumerRecord<String, String> record : records) {
            meldinger.add(JsonDeserialiserer.deserialiser(record.value(), VedtakHendelse.class));
        }
        return meldinger;
    }

    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    public List<VedtakHendelse> lesMeldingerHeltFraStarten() {
        this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        this.kafkaConsumer.seekToBeginning(this.kafkaConsumer.assignment());
        return lesMeldinger();
    }

    private void subscribe() {
        this.kafkaConsumer.subscribe(Collections.singletonList(this.topic));
    }
}
