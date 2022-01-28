package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.producer;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class SelvbetjeningMeldingProducer {

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.registerModule(new JavaTimeModule());
        OM.registerModule(new Jdk8Module());
        OM.registerModule(new SimpleModule());
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Producer<String, String> producer;
    private String topic;

    SelvbetjeningMeldingProducer() {
        // for CDI proxy
    }

    @Inject
    SelvbetjeningMeldingProducer(@KonfigVerdi("tilbakekreving.brukerdialog.hendelse.v1.topic.url") String topic,
                                 @KonfigVerdi("bootstrap.servers") String bootstrapServers,
                                 @KonfigVerdi("systembruker.username") String username,
                                 @KonfigVerdi("systembruker.password") String password) {
        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("client.id", ApplicationName.hvilkenTilbakeAppName());

        setSecurity(username, properties);
        setUsernameAndPassword(username, password, properties);

        this.producer = createProducer(properties);
        this.topic = topic;
    }

    public void sendMelding(SelvbetjeningMelding hendelse) {
        try {
            String verdiSomJson = OM.writeValueAsString(hendelse);
            sendJsonMedNøkkel(hendelse.getNorskIdent(), verdiSomJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kunne ikke serialisere SendtVarselInformasjon til JSON", e);
        }
    }

    private void setUsernameAndPassword(String username, String password, Properties properties) {
        if ((username != null && !username.isEmpty())
                && (password != null && !password.isEmpty())) {
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

    private void sendJsonMedNøkkel(String nøkkel, String json) {
        runProducerWithSingleJson(new ProducerRecord<>(topic, nøkkel, json));
    }

    private void runProducerWithSingleJson(ProducerRecord<String, String> record) {
        try {
            producer.send(record).get();
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
