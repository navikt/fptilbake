package no.nav.foreldrepenger.tilbakekreving.los.klient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;

@ApplicationScoped
public class KafkaProducerAiven extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerAiven.class);
    private static final String CALLID_NAME = "Nav-Tbk-CallId";

    public KafkaProducerAiven() {
        //for CDI proxy
    }

    @Inject
    public KafkaProducerAiven(@KonfigVerdi(value = "kafka.fplos.aiven.topic") String topic) {
        super(topic);
    }


    public void sendHendelse(String nøkkel, String verdi)  {
        runProducerWithSingleJson(new ProducerRecord<>(getTopic(), nøkkel, verdi));
        LOG.info("Melding sendt til Aiven på {} for nøkkel {}", getTopic(), nøkkel);
    }

    public void sendHendelseMedCallId(String nøkkel, String verdi, String callId) {
        var melding = new ProducerRecord<>(getTopic(), null, nøkkel, verdi, new RecordHeaders().add(CALLID_NAME, callId.getBytes()));
        var recordMetadata = runProducerWithSingleJson(melding);
        LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandlingId {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), nøkkel);
    }

}
