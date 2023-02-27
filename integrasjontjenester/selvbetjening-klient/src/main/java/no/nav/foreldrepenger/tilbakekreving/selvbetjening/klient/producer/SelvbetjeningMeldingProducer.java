package no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.kafka.AivenMeldingProducer;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.dto.SelvbetjeningMelding;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class SelvbetjeningMeldingProducer extends AivenMeldingProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SelvbetjeningMeldingProducer.class);

    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();

    public SelvbetjeningMeldingProducer() {
        //for CDI proxy
    }

    @Inject
    public SelvbetjeningMeldingProducer(@KonfigVerdi(value = "kafka.tilbakekreving.brukerdialog.hendelse.v1.topic.url") String topic) {
        super(topic);
    }


    public void sendMelding(SelvbetjeningMelding hendelse) {
        try {
            var nøkkel = hendelse.getNorskIdent();
            var verdiSomJson = MAPPER.writeValueAsString(hendelse);
            var record = new ProducerRecord<>(getTopic(), nøkkel, verdiSomJson);
            var recordMetadata = runProducerWithSingleJson(record);
            LOG.info("Melding sendt til {} partisjon {} offset {} for saksnummer {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset(), hendelse.getSaksnummer());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Kunne ikke serialisere SelvbetjeningMelding til JSON", e);
        }
    }

}