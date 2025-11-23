package no.nav.foreldrepenger.tilbakekreving.los.klient.fp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.vedtak.hendelser.behandling.v1.BehandlingHendelseV1;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class FpLosKafkaProducerAiven extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(FpLosKafkaProducerAiven.class);

    public FpLosKafkaProducerAiven() {
        //for CDI proxy
    }

    @Inject
    public FpLosKafkaProducerAiven(@KonfigVerdi(value = "kafka.fplos.aiven.topic") String topic) {
        super(topic);
    }


    public void sendHendelseFplos(Saksnummer saksnummer, BehandlingHendelseV1 dto)  {
        var nøkkel = saksnummer.getVerdi();
        var verdi = DefaultJsonMapper.toJson(dto);

        runProducerWithSingleJson(new ProducerRecord<>(getTopic(), nøkkel, verdi));
        LOG.info("Melding sendt til Aiven på {} for behandlingId {}", getTopic(), nøkkel);
    }

}
