package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.time.OffsetDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.felles.DvhKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SakshendelserKafkaProducer {

    private DvhKafkaProducer kafkaProducer;
    private String topic;

    SakshendelserKafkaProducer() {
        //for CDI proxy
    }

    @Inject
    public SakshendelserKafkaProducer(DvhKafkaProducer kafkaProducer, @KonfigVerdi(value = "kafka.dvh.saksstatistikk.topic", defaultVerdi = "privat-tilbakekreving-dvh-saksstatistikk-v1") String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    public void sendMelding(BehandlingTilstand hendelse) {
        hendelse.setTekniskTid(OffsetDateTime.now()); //tidspunkt for sending

        String nøkkel = hendelse.getBehandlingUuid().toString();
        String verdi = BehandlingTilstandMapper.tilJsonString(hendelse);
        var melding = new ProducerRecord<>(topic, nøkkel, verdi);
        kafkaProducer.sendMelding(melding);
    }


}
