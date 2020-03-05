package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendSakshendelserTilDvhTask.TASK_TYPE)
public class SendSakshendelserTilDvhTask implements ProsessTaskHandler {
    public static final String TASK_TYPE = "dvh.send.sakshendelser";

    private SakshendelserKafkaProducer kafkaProducer;

    @Inject
    public SendSakshendelserTilDvhTask(SakshendelserKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTilstand behandlingTilstand = BehandlingTilstandMapper.fraJson(prosessTaskData.getPayloadAsString());
        kafkaProducer.sendMelding(behandlingTilstand);
    }


}
