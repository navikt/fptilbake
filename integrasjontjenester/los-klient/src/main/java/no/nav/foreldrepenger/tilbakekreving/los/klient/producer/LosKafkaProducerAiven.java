package no.nav.foreldrepenger.tilbakekreving.los.klient.producer;

import java.io.IOException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.los.klient.TilbakebetalingBehandlingProsessEventMapper;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.hendelser.behandling.v1.BehandlingHendelseV1;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class LosKafkaProducerAiven extends AivenMeldingProducer {

    private static final Logger LOG = LoggerFactory.getLogger(LosKafkaProducerAiven.class);
    private static final String CALLID_NAME = "Nav-Tbk-CallId";

    public LosKafkaProducerAiven() {
        //for CDI proxy
    }

    @Inject
    public LosKafkaProducerAiven(@KonfigVerdi(value = "kafka.los.aiven.topic") String topic) {
        super(topic);
    }


    public void sendHendelse(UUID uuid, TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        String nøkkel = uuid.toString();
        String verdi = TilbakebetalingBehandlingProsessEventMapper.getJson(behandlingProsessEventDto);
        String callId = MDCOperations.getCallId() != null ? MDCOperations.getCallId() : MDCOperations.generateCallId();

        ProducerRecord<String, String> melding = new ProducerRecord<>(getTopic(), null, nøkkel, verdi, new RecordHeaders().add(CALLID_NAME, callId.getBytes()));

        RecordMetadata recordMetadata = runProducerWithSingleJson(melding);
        LOG.info("Melding sendt til Aiven på {} partisjon {} offset {} for behandlingId {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), nøkkel);
    }

    public void sendHendelseFplos(Saksnummer saksnummer, BehandlingHendelseV1 dto)  {
        String nøkkel = saksnummer.getVerdi();
        String verdi = DefaultJsonMapper.toJson(dto);

        runProducerWithSingleJson(new ProducerRecord<>(getTopic(), nøkkel, verdi));
        LOG.info("Melding sendt til Aiven på {} for behandlingId {}", getTopic(), nøkkel);
    }

}
