package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;

@ApplicationScoped
public class AivenEttersendelserKafkaProducer extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(AivenEttersendelserKafkaProducer.class);

    public AivenEttersendelserKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public AivenEttersendelserKafkaProducer(@KonfigVerdi(value = "kafka.dvh.ettersendelse.aiven.topic") String topic) {
        super(topic);
    }

    public void sendMelding(BehandlingTilstand hendelse) {
        hendelse.setTekniskTid(OffsetDateTime.now(ZoneOffset.UTC)); //tidspunkt for sending

        var nøkkel = hendelse.getBehandlingUuid().toString();
        var verdi = BehandlingTilstandMapper.tilJsonString(hendelse);
        var melding = new ProducerRecord<>(getTopic(), nøkkel, verdi);
        var recordMetadata = runProducerWithSingleJson(melding);
        if (LOG.isInfoEnabled()) {
            LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandling {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), hendelse.getBehandlingUuid());
        }
    }
}
