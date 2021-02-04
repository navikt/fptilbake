package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

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

import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.YtelsesvedtakHendelseConsumer;
import no.nav.foreldrepenger.tilbakekreving.kafka.util.JsonDeserialiserer;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TilkjentYtelseMeldingConsumer extends YtelsesvedtakHendelseConsumer {

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    TilkjentYtelseMeldingConsumer() {
        // CDI
    }

    @Inject
    public TilkjentYtelseMeldingConsumer(@KonfigVerdi(FEED_URL) String topic,
                                         @KonfigVerdi(BOOTSTRAP_SERVERS) String bootstrapServers,
                                         @KonfigVerdi("app.name") String applikasjonNavn,
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
    public List<TilkjentYtelseMelding> lesMeldinger() {
        List<TilkjentYtelseMelding> meldinger = new ArrayList<>();
        ConsumerRecords<String, String> records = this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        for (ConsumerRecord<String, String> record : records) {
            meldinger.add(JsonDeserialiserer.deserialiser(record.value(), TilkjentYtelseMelding.class));
        }
        return meldinger;
    }

    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    public List<TilkjentYtelseMelding> lesMeldingerHeltFraStarten() {
        this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        this.kafkaConsumer.seekToBeginning(this.kafkaConsumer.assignment());
        return lesMeldinger();
    }

    private void subscribe() {
        this.kafkaConsumer.subscribe(Collections.singletonList(this.topic));
    }

}
