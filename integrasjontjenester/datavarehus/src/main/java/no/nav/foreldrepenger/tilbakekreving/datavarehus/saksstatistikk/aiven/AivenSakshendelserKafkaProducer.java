package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;

@ApplicationScoped
public class AivenSakshendelserKafkaProducer extends AivenMeldingProducer {

    public AivenSakshendelserKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public AivenSakshendelserKafkaProducer(@KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                                           @KonfigVerdi(value = "kafka.dvh.sakshendelse.aiven.topic") String topic,
                                           @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
                                           @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String trustStorePassword,
                                           @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStorePath,
                                           @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keyStorePassword,
                                           @KonfigVerdi(value = "KAFKA_OVERRIDE_KEYSTORE_PASSWORD", required = false) String vtpOverride) {
        super(topic, bootstrapServers, "KP-" + topic, trustStorePath, trustStorePassword, keyStorePath, keyStorePassword, vtpOverride);
    }

    public void sendMelding(BehandlingTilstand hendelse) {
        hendelse.setTekniskTid(OffsetDateTime.now(ZoneOffset.UTC)); //tidspunkt for sending

        String nøkkel = hendelse.getBehandlingUuid().toString();
        String verdi = BehandlingTilstandMapper.tilJsonString(hendelse);
        var melding = new ProducerRecord<>(getTopic(), nøkkel, verdi);
        runProducerWithSingleJson(melding);
    }
}
