package no.nav.foreldrepenger.tilbakekreving.hendelser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class VedtakFattetMeldingConsumerAiven {

    private static final Logger logger = LoggerFactory.getLogger(VedtakFattetMeldingConsumerAiven.class);
    protected static final int TIMEOUT = 1000;

    private KafkaConsumer<String, String> kafkaConsumer;
    private String topic;

    VedtakFattetMeldingConsumerAiven() {
        // CDI
    }

    @Inject
    public VedtakFattetMeldingConsumerAiven(@KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                                            @KonfigVerdi(value = "kafka.fattevedtak.aiven.topic") String topic,
                                            @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String truststorePath,
                                            @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String truststorePassword,
                                            @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keystorePath,
                                            @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keystorePassword,
                                            @KonfigVerdi(value = "KAFKA_OVERRIDE_KEYSTORE_PASSWORD", required = false) String vtpOverride) {

        this.topic = topic;

        if (vtpOverride != null) {
            keystorePassword = vtpOverride;
        }

        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("group.id", ApplicationName.hvilkenTilbakeAppName());
        properties.setProperty("client.id", "KC-" + ApplicationName.hvilkenTilbakeAppName());
        properties.setProperty("enable.auto.commit", "false"); //det gjøres manuelt commit (se manualCommitSync i denne klassen)
        properties.setProperty("max.poll.records", "20");
        properties.setProperty("auto.offset.reset", "earliest"); //TODO endre til none/latest når har fått lest noe fra topic
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());

        // Security
        if (vtpOverride != null) {
            properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            properties.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, "vtp", "vtp");
            properties.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
        } else {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks");
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
            properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        }

        this.kafkaConsumer = new KafkaConsumer<>(properties);
        this.subscribe();
    }

    @Inject
    public List<Ytelse> lesMeldinger() {
        List<Ytelse> meldinger = new ArrayList<>();
        ConsumerRecords<String, String> records = this.kafkaConsumer.poll(Duration.ofMillis(TIMEOUT));
        if (records.isEmpty()) {
            logger.debug("Leste {} meldinger fra {}", records.count(), topic);
        } else {
            logger.info("Leste {} meldinger fra {}", records.count(), topic);
        }
        for (ConsumerRecord<String, String> record : records) {
            Ytelse innhold = DefaultJsonMapper.fromJson(record.value(), Ytelse.class);
            meldinger.add(innhold);
            logger.info("Leste vedtakhendelse fra {} partition {} offset {}. Saknummer {} ytelsetype {}", record.topic(), record.partition(), record.offset(), innhold.getSaksnummer(), innhold.getYtelse());
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
