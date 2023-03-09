package no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.RetriableException;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;

public abstract class AivenMeldingProducer {

    private Producer<String, String> producer;
    private String topic;

    protected AivenMeldingProducer(String topic) {

        this.topic = topic;
        this.producer = new KafkaProducer<>(KafkaProperties.forProducer());
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

    protected RecordMetadata runProducerWithSingleJson(ProducerRecord<String, String> producerRecord) {
        try {
            return producer.send(producerRecord)
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
