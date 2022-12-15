package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;

@ApplicationScoped
public class SelvbetjeningMeldingProducer extends AivenMeldingProducer {
    private static final Logger logger = LoggerFactory.getLogger(SelvbetjeningMeldingProducer.class);

    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();

    public SelvbetjeningMeldingProducer() {
        //for CDI proxy
    }

    @Inject
    public SelvbetjeningMeldingProducer(@KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                                        @KonfigVerdi(value = "kafka.tilbakekreving.brukerdialog.hendelse.v1.topic.url") String topic,
                                        @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
                                        @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String trustStorePassword,
                                        @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStorePath,
                                        @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keyStorePassword,
                                        @KonfigVerdi(value = "KAFKA_OVERRIDE_KEYSTORE_PASSWORD", required = false) String vtpOverride) {
        super(topic, bootstrapServers, "fptilbake" + topic, trustStorePath, trustStorePassword, keyStorePath, keyStorePassword, vtpOverride);
    }


    public void sendMelding(SelvbetjeningMelding hendelse) {
        try {
            var nøkkel = hendelse.getNorskIdent();
            var verdiSomJson = MAPPER.writeValueAsString(hendelse);
            var record = new ProducerRecord<>(getTopic(), nøkkel, verdiSomJson);
            var recordMetadata = runProducerWithSingleJson(record);
            logger.info("Melding sendt til {} partisjon {} offset {} for saksnummer {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), hendelse.getSaksnummer());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kunne ikke serialisere SelvbetjeningMelding til JSON", e);
        }
    }

}