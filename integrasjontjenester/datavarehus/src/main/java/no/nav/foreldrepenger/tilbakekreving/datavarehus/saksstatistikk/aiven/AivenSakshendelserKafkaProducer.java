package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;

@ApplicationScoped
public class AivenSakshendelserKafkaProducer extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(AivenSakshendelserKafkaProducer.class);

    public AivenSakshendelserKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public AivenSakshendelserKafkaProducer(@KonfigVerdi(value = "kafka.dvh.sakshendelse.aiven.topic") String topic) {
        super(topic);
    }

    public void sendMelding(BehandlingTilstand hendelse) {
        hendelse.setTekniskTid(OffsetDateTime.now(ZoneOffset.UTC)); //tidspunkt for sending

        String nøkkel = hendelse.getBehandlingUuid().toString();
        String verdi = BehandlingTilstandMapper.tilJsonString(hendelse);
        var melding = new ProducerRecord<>(getTopic(), nøkkel, verdi);
        RecordMetadata recordMetadata = runProducerWithSingleJson(melding);
        LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandling {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), hendelse.getBehandlingUuid());
    }
}
