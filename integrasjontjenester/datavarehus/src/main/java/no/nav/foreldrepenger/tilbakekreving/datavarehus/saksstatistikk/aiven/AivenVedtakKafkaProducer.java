package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;

@ApplicationScoped
public class AivenVedtakKafkaProducer extends AivenMeldingProducer {

    private static final Logger logger = LoggerFactory.getLogger(AivenVedtakKafkaProducer.class);

    public AivenVedtakKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public AivenVedtakKafkaProducer(@KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                                    @KonfigVerdi(value = "kafka.dvh.vedtak.aiven.topic") String topic,
                                    @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
                                    @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String trustStorePassword,
                                    @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStorePath,
                                    @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String keyStorePassword,
                                    @KonfigVerdi(value = "KAFKA_OVERRIDE_KEYSTORE_PASSWORD", required = false) String vtpOverride) {
        super(topic, bootstrapServers, "KP-" + topic, trustStorePath, trustStorePassword, keyStorePath, keyStorePassword, vtpOverride);
    }

    public void sendMelding(VedtakOppsummering vedtakOppsummering) {
        String nøkkel = vedtakOppsummering.getBehandlingUuid().toString();
        String verdi = VedtakOppsummeringMapper.tilJsonString(vedtakOppsummering);
        var melding = new ProducerRecord<>(getTopic(), nøkkel, verdi);
        runProducerWithSingleJson(melding);
    }

}
