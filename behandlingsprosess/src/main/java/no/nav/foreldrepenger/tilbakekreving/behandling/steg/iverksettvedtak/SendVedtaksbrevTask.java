package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.VedtaksbrevTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendVedtaksbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendVedtaksbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "iverksetteVedtak.sendVedtaksbrev";

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevTask.class);

    private VergeRepository vergeRepository;
    private VedtaksbrevTjeneste vedtaksbrevTjeneste;

    SendVedtaksbrevTask() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevTask(VergeRepository vergeRepository,
                               VedtaksbrevTjeneste vedtaksbrevTjeneste) {
        this.vergeRepository = vergeRepository;
        this.vedtaksbrevTjeneste = vedtaksbrevTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        if (vergeRepository.finnesVerge(behandlingId)) {
            vedtaksbrevTjeneste.sendVedtaksbrev(behandlingId, BrevMottaker.VERGE);
        }
        vedtaksbrevTjeneste.sendVedtaksbrev(behandlingId, BrevMottaker.BRUKER);
        log.info("Utført for behandling: {}", behandlingId);
    }
}
