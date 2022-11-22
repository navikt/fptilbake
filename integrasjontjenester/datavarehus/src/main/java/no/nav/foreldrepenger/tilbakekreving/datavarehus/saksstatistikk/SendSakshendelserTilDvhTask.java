package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven.AivenSakshendelserKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.onprem.SakshendelserKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("dvh.send.sakshendelser")
public class SendSakshendelserTilDvhTask implements ProsessTaskHandler {

    private SakshendelserKafkaProducer kafkaProducer;
    private AivenSakshendelserKafkaProducer aivenSakshendelserKafkaProducer;
    private boolean brukAiven;

    @Inject
    public SendSakshendelserTilDvhTask(@KonfigVerdi(value = "toggle.aiven.dvh", defaultVerdi = "true") boolean brukAiven,
                                       SakshendelserKafkaProducer kafkaProducer,
                                       AivenSakshendelserKafkaProducer aivenSakshendelserKafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.brukAiven = brukAiven;
        this.aivenSakshendelserKafkaProducer = aivenSakshendelserKafkaProducer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTilstand behandlingTilstand = BehandlingTilstandMapper.fraJson(prosessTaskData.getPayloadAsString());

        if (brukAiven) {
            aivenSakshendelserKafkaProducer.sendMelding(behandlingTilstand);
        } else {
            kafkaProducer.sendMelding(behandlingTilstand);
        }
    }

}
