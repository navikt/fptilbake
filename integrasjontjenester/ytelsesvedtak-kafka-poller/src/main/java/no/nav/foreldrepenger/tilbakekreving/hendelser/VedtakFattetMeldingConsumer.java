package no.nav.foreldrepenger.tilbakekreving.hendelser;

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

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class VedtakFattetMeldingConsumer {

    protected static final int TIMEOUT = 1000;

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    VedtakFattetMeldingConsumer() {
        // CDI
    }

    @Inject
    public VedtakFattetMeldingConsumer(@KonfigVerdi("kafka.fattevedtak.topic") String topic,
                                       @KonfigVerdi("kafka.bootstrap.servers") String bootstrapServers,
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
            meldinger.add(DefaultJsonMapper.fromJson(record.value(), Ytelse.class));
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

    private Properties lagFellesProperty(String bootstrapServers, String applikasjonNavn) {
        Properties properties = new Properties();
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("group.id", applikasjonNavn);
        properties.setProperty("client.id", applikasjonNavn);
        properties.setProperty("enable.auto.commit", "false");
        properties.setProperty("max.poll.records", "20");
        properties.setProperty("auto.offset.reset", "latest");
        return properties;
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

    private boolean notNullNotEmpty(String str) {
        return (str != null && !str.isEmpty());
    }
}
