package no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class FplosKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(FplosKafkaProducer.class);
    private static final String CALLID_NAME = "Nav-Tbk-CallId";

    private Producer<String, String> producer;
    private String topic;


    FplosKafkaProducer() {
        // for CDI proxy
    }

    @Inject
    public FplosKafkaProducer(@KonfigVerdi("kafka.fplos.topic") String topic,
                              @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                              @KonfigVerdi("app.name") String clientId,
                              @KonfigVerdi("systembruker.username") String username,
                              @KonfigVerdi("systembruker.password") String password) {
        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("client.id", clientId);
        properties.setProperty("max.in.flight.requests.per.connection", "1"); //påkrevet for å garantere rekkefølge sammen med retries
        properties.setProperty("acks", "all"); //mindre sjangse for å miste melding

        setSecurity(username, properties);
        setUsernameAndPassword(username, password, properties);

        this.producer = createProducer(properties);
        this.topic = topic;

    }

    public void sendJsonMedNøkkel(String nøkkel, String json) {
        String callId = MDCOperations.getCallId() != null ? MDCOperations.getCallId() : MDCOperations.generateCallId();
        runProducerWithSingleJson(new ProducerRecord<>(topic, null, nøkkel, json, new RecordHeaders().add(CALLID_NAME, callId.getBytes())));
    }

    private void runProducerWithSingleJson(ProducerRecord<String, String> record) {
        try {
            producer.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw kafkaPubliseringException(e);
        } catch (Exception e) {
            throw kafkaPubliseringException(e);
        }
    }

    private IntegrasjonException kafkaPubliseringException(Exception e) {
        return new IntegrasjonException("FP-HENDELSE-925476", "Uventet feil ved sending til Kafka, topic " + topic, e);
    }

    private void setUsernameAndPassword(String username, String password, Properties properties) {
        if ((username != null && !username.isEmpty()) && (password != null && !password.isEmpty())) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.setProperty("sasl.jaas.config", jaasCfg);
        }
    }

    private Producer<String, String> createProducer(Properties properties) {
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }

    private void setSecurity(String username, Properties properties) {
        if (username != null && !username.isEmpty()) {
            properties.setProperty("security.protocol", "SASL_SSL");
            properties.setProperty("sasl.mechanism", "PLAIN");
        }
    }
}
