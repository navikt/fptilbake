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

import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.StringUtils;

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
                                 @KonfigVerdi("app.name") String clientId,
                                 @KonfigVerdi("systembruker.username") String username,
                                 @KonfigVerdi("systembruker.password") String password) {
        Properties properties = new Properties();

        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("client.id", clientId);

        setSecurity(username, properties);
        setUsernameAndPassword(username, password, properties);

        this.producer = createProducer(properties);
        this.topic = topic;
    }

    void setUsernameAndPassword(String username, String password, Properties properties) {
        if (!StringUtils.nullOrEmpty(username) && !StringUtils.nullOrEmpty(password)) {
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, username, password);
            properties.setProperty("sasl.jaas.config", jaasCfg);
        }
    }

    Producer<String, String> createProducer(Properties properties) {
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }

    void setSecurity(String username, Properties properties) {
        if (!StringUtils.nullOrEmpty(username)) {
            properties.setProperty("security.protocol", "SASL_SSL");
            properties.setProperty("sasl.mechanism", "PLAIN");
        }
    }

    public void flush() {
        producer.flush();
    }

    public void sendMelding(SelvbetjeningMelding hendelse) {
        try {
            String verdiSomJson = OM.writeValueAsString(hendelse);
            sendJsonMedNøkkel(hendelse.getNorskIdent(), verdiSomJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kunne ikke serialisere SendtVarselInformasjon til JSON", e);
        }
    }

    private void sendJsonMedNøkkel(String nøkkel, String json) {
        runProducerWithSingleJson(new ProducerRecord<>(topic, nøkkel, json));
    }

    void runProducerWithSingleJson(ProducerRecord<String, String> record) {
        try {
            @SuppressWarnings("unused")
            var recordMetadata = producer.send(record).get(); // NOSONAR
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SendVarselhendelseFeil.FACTORY.uventetFeilKafka(topic, e).toException();
        } catch (ExecutionException e) {
            throw SendVarselhendelseFeil.FACTORY.uventetFeilKafka(topic, e).toException();
        } catch (AuthenticationException | AuthorizationException e) {
            throw SendVarselhendelseFeil.FACTORY.påloggingsfeilKafka(topic, e).toException();
        } catch (RetriableException e) {
            throw SendVarselhendelseFeil.FACTORY.midlertidigFeilKafka(topic, e).toException();
        } catch (KafkaException e) {
            throw SendVarselhendelseFeil.FACTORY.feilMedKafka(topic, e).toException();
        }
    }

    interface SendVarselhendelseFeil extends DeklarerteFeil {

        SendVarselhendelseFeil FACTORY = FeilFactory.create(SendVarselhendelseFeil.class);

        @TekniskFeil(feilkode = "FPT-151561", feilmelding = "Uventet feil ved sending til Kafka for topic %s", logLevel = LogLevel.WARN)
        Feil uventetFeilKafka(String topic, Exception cause);

        @ManglerTilgangFeil(feilkode = "FPT-732111", feilmelding = "Feil med pålogging mot Kafka for topic %s", logLevel = LogLevel.WARN)
        Feil påloggingsfeilKafka(String topic, Exception cause);

        @TekniskFeil(feilkode = "FPT-682119", feilmelding = "Midlertidig feil ved sending til Kafka, vil prøve igjen. Gjelder topic %s", logLevel = LogLevel.WARN)
        Feil midlertidigFeilKafka(String topic, Exception cause);

        @TekniskFeil(feilkode = "FPT-981074", feilmelding = "Uventet feil ved sending til Kafka for topic %s", logLevel = LogLevel.WARN)
        Feil feilMedKafka(String topic, Exception cause);

    }

}
