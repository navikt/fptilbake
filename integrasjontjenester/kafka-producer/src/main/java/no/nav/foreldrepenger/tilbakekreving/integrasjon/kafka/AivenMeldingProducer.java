package no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringSerializer;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;

public abstract class AivenMeldingProducer {

    private Producer<String, String> producer;
    private String topic;

    protected AivenMeldingProducer(String topic,
                         String bootstrapServers,
                         String clientId,
                         String truststorePath,
                         String truststorePassword,
                         String keystorePath,
                         String keystorePassword,
                         String vtpOverride) {

        this.topic = topic;

        if(vtpOverride != null) {
            keystorePassword = vtpOverride;
        }

        Properties properties = new Properties();

        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("client.id", clientId);

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Security
        if(vtpOverride != null) {
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

        this.producer = new KafkaProducer<>(properties);
    }

    protected AivenMeldingProducer() {
    }

    protected String getTopic() {
        return topic;
    }

    public void flushAndClose() {
        producer.flush();
        producer.close();
    }

    public void flush() {
        producer.flush();
    }

    protected RecordMetadata runProducerWithSingleJson(ProducerRecord<String, String> record) {
        try {
            return producer.send(record)
                .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw uventetFeilKafka(topic, e);
        } catch (ExecutionException e) {
            throw uventetFeilKafka(topic, e);
        } catch (AuthenticationException | AuthorizationException e) {
            throw påloggingsfeilKafka(topic, e);
        } catch (RetriableException e) {
            throw midlertidigFeilKafka(topic, e);
        } catch (KafkaException e) {
            throw feilMedKafka(topic, e);
        }
    }

    private static TekniskException uventetFeilKafka(String topic, Exception cause) {
        return new TekniskException("FPT-151561", String.format("Uventet feil ved sending til Kafka for topic %s", topic), cause);
    }

    private static ManglerTilgangException påloggingsfeilKafka(String topic, Exception cause) {
        return new ManglerTilgangException("FPT-732111", String.format("Feil med pålogging mot Kafka for topic %s", topic), cause);
    }

    private static TekniskException midlertidigFeilKafka(String topic, Exception cause) {
        return new TekniskException("FPT-682119", String.format("Midlertidig feil ved sending til Kafka, vil prøve igjen. Gjelder topic %s", topic), cause);
    }

    private static TekniskException feilMedKafka(String topic, Exception cause) {
        return new TekniskException("FPT-981074", String.format("Uventet feil ved sending til Kafka for topic %s", topic), cause);
    }


}
