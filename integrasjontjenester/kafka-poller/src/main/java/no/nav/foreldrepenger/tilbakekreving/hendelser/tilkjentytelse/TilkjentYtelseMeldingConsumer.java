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
import org.apache.kafka.common.serialization.StringDeserializer;

import no.nav.foreldrepenger.tilbakekreving.kafka.util.JsonDeserialiserer;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TilkjentYtelseMeldingConsumer {

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    TilkjentYtelseMeldingConsumer() {
        // CDI
    }

    @Inject
    public TilkjentYtelseMeldingConsumer(@KonfigVerdi(FeedProperties.FEED_URL) String topic,
                                         @KonfigVerdi(FeedProperties.BOOTSTRAP_SERVERS) String bootstrapServers,
                                         @KonfigVerdi("application.name") String applikasjonNavn,
                                         @KonfigVerdi("systembruker.username") String username,
                                         @KonfigVerdi("systembruker.password") String password) {
        Properties properties = new Properties();
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("group.id", applikasjonNavn);
        properties.setProperty("client.id", applikasjonNavn);
        properties.setProperty("enable.auto.commit", "false");
        properties.setProperty("max.poll.records", "20");
        properties.setProperty("auto.offset.reset", "earliest"); // TODO sett til 'none' når det har blitt lest fra køen i produksjon
        this.setSecurity(username, properties);
        this.addUserToProperties(username, password, properties);
        this.kafkaConsumer = new KafkaConsumer<>(properties);
        this.topic = topic;
        this.subscribe();
    }

    @Inject
    public List<TilkjentYtelseMelding> lesMeldinger() {
        List<TilkjentYtelseMelding> meldinger = new ArrayList<>();
        ConsumerRecords<String, String> records = this.kafkaConsumer.poll(Duration.ofMillis(FeedProperties.TIMEOUT));
        for (ConsumerRecord<String, String> record : records) {
            meldinger.add(JsonDeserialiserer.deserialiser(record.value(), TilkjentYtelseMelding.class));
        }
        return meldinger;
    }

    public void manualCommitSync() {
        kafkaConsumer.commitSync();
    }

    public List<TilkjentYtelseMelding> lesMeldingerHeltFraStarten() {
        this.kafkaConsumer.poll(Duration.ofMillis(FeedProperties.TIMEOUT));
        this.kafkaConsumer.seekToBeginning(this.kafkaConsumer.assignment());
        return lesMeldinger();
    }

    private void setSecurity(String username, Properties properties) {
        if (username != null && !username.isEmpty()) {
            properties.setProperty("security.protocol", "SASL_SSL");
            properties.setProperty("sasl.mechanism", "PLAIN");
        }
    }

    private void addUserToProperties(@KonfigVerdi("kafka.username") String username, @KonfigVerdi("kafka.password") String password, Properties properties) {
        if (notNullNotEmpty(username) && notNullNotEmpty(password)) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.setProperty("sasl.jaas.config", jaasCfg);
        }
    }

    private void subscribe() {
        this.kafkaConsumer.subscribe(Collections.singletonList(this.topic));
    }

    private boolean notNullNotEmpty(String str) {
        return (str != null && !str.isEmpty());
    }

}
