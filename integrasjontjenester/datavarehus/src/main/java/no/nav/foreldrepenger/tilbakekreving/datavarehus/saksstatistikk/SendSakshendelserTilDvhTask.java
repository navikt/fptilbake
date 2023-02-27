package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven.AivenSakshendelserKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("dvh.send.sakshendelser")
public class SendSakshendelserTilDvhTask implements ProsessTaskHandler {

    private AivenSakshendelserKafkaProducer aivenSakshendelserKafkaProducer;

    @Inject
    public SendSakshendelserTilDvhTask(AivenSakshendelserKafkaProducer aivenSakshendelserKafkaProducer) {
        this.aivenSakshendelserKafkaProducer = aivenSakshendelserKafkaProducer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTilstand behandlingTilstand = BehandlingTilstandMapper.fraJson(prosessTaskData.getPayloadAsString());
        aivenSakshendelserKafkaProducer.sendMelding(behandlingTilstand);
    }

}
