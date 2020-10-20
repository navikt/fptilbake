package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.felles.DvhKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class VedtakOppsummeringKafkaProducer {

    private DvhKafkaProducer kafkaProducer;
    private String topic;

    VedtakOppsummeringKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public VedtakOppsummeringKafkaProducer(DvhKafkaProducer kafkaProducer, @KonfigVerdi(value = "kafka.dvh.vedtak.topic") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    public void sendMelding(VedtakOppsummering vedtakOppsummering) {
        String nøkkel = vedtakOppsummering.getBehandlingUuid().toString();
        String verdi = VedtakOppsummeringMapper.tilJsonString(vedtakOppsummering);
        var melding = new ProducerRecord<>(topic, nøkkel, verdi);
        kafkaProducer.sendMelding(melding);
    }


}
