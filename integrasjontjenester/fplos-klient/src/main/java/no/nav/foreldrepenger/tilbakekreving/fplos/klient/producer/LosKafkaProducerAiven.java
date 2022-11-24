package no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.TilbakebetalingBehandlingProsessEventMapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class LosKafkaProducerAiven extends AivenMeldingProducer {

    private static final Logger logger = LoggerFactory.getLogger(LosKafkaProducerAiven.class);
    private static final String CALLID_NAME = "Nav-Tbk-CallId";
    private static final String HEARTHBEAT_HEADER = "Hearthbeat";

    public LosKafkaProducerAiven() {
        //for CDI proxy
    }

    @Inject
    public LosKafkaProducerAiven(@KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                                 @KonfigVerdi(value = "kafka.los.aiven.topic") String topic,
                                 @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
                                 @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String trustStorePassword,
                                 @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStorePath,
                                 @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keyStorePassword,
                                 @KonfigVerdi(value = "KAFKA_OVERRIDE_KEYSTORE_PASSWORD", required = false) String vtpOverride) {
        super(topic, bootstrapServers, "KP-" + topic, trustStorePath, trustStorePassword, keyStorePath, keyStorePassword, vtpOverride);
    }

    public void sendHendelse(UUID uuid, TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        String nøkkel = uuid.toString();
        String verdi = TilbakebetalingBehandlingProsessEventMapper.getJson(behandlingProsessEventDto);
        String callId = MDCOperations.getCallId() != null ? MDCOperations.getCallId() : MDCOperations.generateCallId();

        ProducerRecord<String, String> melding = new ProducerRecord<>(getTopic(), null, nøkkel, verdi, new RecordHeaders().add(CALLID_NAME, callId.getBytes()));

        RecordMetadata recordMetadata = runProducerWithSingleJson(melding);
        logger.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandlingId {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), behandlingProsessEventDto.getBehandlingId());
    }

    public void sendHearthbeat() {
        String nøkkel = null;
        String verdi = LocalDateTime.now().toInstant(ZoneOffset.UTC).toString();
        ProducerRecord<String, String> melding = new ProducerRecord<>(getTopic(), null, nøkkel, verdi, new RecordHeaders().add(HEARTHBEAT_HEADER, null));
        RecordMetadata recordMetadata = runProducerWithSingleJson(melding);
        logger.info("Sendt hearthbeat til Aiven på {} partisjon {} offset {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
    }

}
