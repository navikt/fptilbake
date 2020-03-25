package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendVedtakHendelserTilDvhTask.TASKTYPE)
public class SendVedtakHendelserTilDvhTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "dvh.send.vedtak.hendelser";

    private VedtakOppsummeringKafkaProducer kafkaProducer;

    SendVedtakHendelserTilDvhTask(){
        // for CDI
    }

    @Inject
    public SendVedtakHendelserTilDvhTask(VedtakOppsummeringKafkaProducer kafkaProducer){
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        VedtakOppsummering vedtakOppsummering = VedtakOppsummeringMapper.fraJson(prosessTaskData.getPayloadAsString());
        kafkaProducer.sendMelding(vedtakOppsummering);
    }
}
