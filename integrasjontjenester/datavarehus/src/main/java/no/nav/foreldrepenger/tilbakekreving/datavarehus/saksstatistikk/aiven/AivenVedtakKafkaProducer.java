package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;

@ApplicationScoped
public class AivenVedtakKafkaProducer extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(AivenVedtakKafkaProducer.class);

    public AivenVedtakKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public AivenVedtakKafkaProducer(@KonfigVerdi(value = "kafka.dvh.vedtak.aiven.topic") String topic) {
        super(topic);
    }

    public void sendMelding(VedtakOppsummering vedtakOppsummering) {
        vedtakOppsummering.setTekniskTid(OffsetDateTime.now(ZoneOffset.UTC)); //tidspunkt for sending

        var nøkkel = vedtakOppsummering.getBehandlingUuid().toString();
        var verdi = VedtakOppsummeringMapper.tilJsonString(vedtakOppsummering);
        var melding = new ProducerRecord<>(getTopic(), nøkkel, verdi);
        var recordMetadata = runProducerWithSingleJson(melding);
        if (LOG.isInfoEnabled()) {
            LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandling {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), vedtakOppsummering.getBehandlingUuid());
        }
    }

}
