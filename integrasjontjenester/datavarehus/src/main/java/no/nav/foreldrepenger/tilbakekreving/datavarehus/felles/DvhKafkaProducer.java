package no.nav.foreldrepenger.tilbakekreving.datavarehus.felles;

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
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

@ApplicationScoped
public class DvhKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(DvhKafkaProducer.class);

    private Producer<String, String> producer;

    DvhKafkaProducer() {
        // for CDI proxy
    }

    @Inject
    public DvhKafkaProducer(@KonfigVerdi("bootstrap.servers") String bootstrapServers,
                            @KonfigVerdi("app.name") String clientId,
                            @KonfigVerdi("systembruker.username") String username,
                            @KonfigVerdi("systembruker.password") String password) {

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("client.id", clientId);
        properties.setProperty("max.in.flight.requests.per.connection", "1"); //påkrevet for å garantere rekkefølge sammen med retries
        properties.setProperty("acks", "all"); //mindre sjangse for å miste melding

        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        setSecurity(username, properties);
        setUsernameAndPassword(username, password, properties);

        this.producer = new KafkaProducer<>(properties);
    }

    public void sendMelding(ProducerRecord<String, String> melding) {
        String topic = melding.topic();
        try {
            producer.send(melding).get();
            producer.flush(); //påkrevd for å sikre at prosesstask feiler hvis sending til kafka feiler
        } catch (InterruptedException e) {
            log.warn("Uventet feil ved sending til Kafka, topic:" + topic, e);
            Thread.currentThread().interrupt(); // reinterrupt
        } catch (ExecutionException e) {
            log.warn("Uventet feil ved sending til Kafka, topic:" + topic, e);
        } catch (AuthenticationException | AuthorizationException e) {
            log.error("Feil i pålogging mot Kafka, topic:" + topic, e);
        } catch (RetriableException e) {
            log.warn("Fikk transient feil mot Kafka, kan prøve igjen, topic:" + topic, e);
        } catch (KafkaException e) {
            log.warn("Fikk feil mot Kafka, topic:" + topic, e);
        }
    }

    void setUsernameAndPassword(String username, String password, Properties properties) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Mangler konfigurasjon for brukernavn mot Kafka");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mangler konfigurasjon for passord mot Kafka");
        }
        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);
        properties.setProperty("sasl.jaas.config", jaasCfg);
    }

    void setSecurity(String username, Properties properties) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Mangler konfigurasjon for brukernavn mot Kafka");
        }
        properties.setProperty("security.protocol", "SASL_SSL");
        properties.setProperty("sasl.mechanism", "PLAIN");
    }
}
