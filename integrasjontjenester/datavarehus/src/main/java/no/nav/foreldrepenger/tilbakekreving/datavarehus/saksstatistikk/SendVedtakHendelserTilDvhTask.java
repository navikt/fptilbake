package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask.UtvidetProsessTaskRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.VedtakOppsummeringMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak.VedtakOppsummering;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendVedtakHendelserTilDvhTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVedtakHendelserTilDvhTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "dvh.send.vedtak";

    private UtvidetProsessTaskRepository prosessTaskRepository;
    private VedtakOppsummeringTjeneste vedtakOppsummeringTjeneste;
    private VedtakOppsummeringKafkaProducer kafkaProducer;

    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    SendVedtakHendelserTilDvhTask() {
        // for CDI
    }

    @Inject
    public SendVedtakHendelserTilDvhTask(UtvidetProsessTaskRepository prosessTaskRepository,
                                         VedtakOppsummeringTjeneste vedtakOppsummeringTjeneste,
                                         VedtakOppsummeringKafkaProducer kafkaProducer) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.vedtakOppsummeringTjeneste = vedtakOppsummeringTjeneste;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingId();
        VedtakOppsummering vedtakOppsummering = vedtakOppsummeringTjeneste.hentVedtakOppsummering(behandlingId);
        validate(vedtakOppsummering);
        kafkaProducer.sendMelding(vedtakOppsummering);
        prosessTaskRepository.oppdaterTaskPayload(prosessTaskData.getId(), VedtakOppsummeringMapper.tilJsonString(vedtakOppsummering));
    }

    private void validate(Object object) {
        var valideringsfeil = validator.validate(object);
        if (!valideringsfeil.isEmpty()) {
            throw new IllegalArgumentException("Valideringsfeil for " + object.getClass().getName() + ": Valideringsfeil:" + valideringsfeil);
        }
    }
}
