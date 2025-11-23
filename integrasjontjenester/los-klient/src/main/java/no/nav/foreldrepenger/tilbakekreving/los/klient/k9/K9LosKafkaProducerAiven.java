package no.nav.foreldrepenger.tilbakekreving.los.klient.k9;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.los.klient.k9.kontrakt.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class K9LosKafkaProducerAiven extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(K9LosKafkaProducerAiven.class);
    private static final String CALLID_NAME = "Nav-Tbk-CallId";

    public K9LosKafkaProducerAiven() {
        //for CDI proxy
    }

    @Inject
    public K9LosKafkaProducerAiven(@KonfigVerdi(value = "kafka.los.aiven.topic") String topic) {
        super(topic);
    }


    public void sendHendelse(UUID uuid, TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        var nøkkel = uuid.toString();
        var verdi = TilbakebetalingBehandlingProsessEventMapper.getJson(behandlingProsessEventDto);
        var callId = Optional.ofNullable(MDCOperations.getCallId()).orElseGet(MDCOperations::generateCallId);

        var melding = new ProducerRecord<String, String>(getTopic(), null, nøkkel, verdi, new RecordHeaders().add(CALLID_NAME, callId.getBytes()));

        var recordMetadata = runProducerWithSingleJson(melding);
        LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandlingId {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), nøkkel);
    }

}
