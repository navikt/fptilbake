package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.BestillDokumentTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendVedtaksbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendVedtaksbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "iverksetteVedtak.sendVedtaksbrev";

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevTask.class);

    private BestillDokumentTjeneste bestillDokumentTjeneste;

    SendVedtaksbrevTask() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevTask(BestillDokumentTjeneste bestillDokumentTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        this.bestillDokumentTjeneste = bestillDokumentTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        bestillDokumentTjeneste.sendVedtaksbrev(prosessTaskData.getFagsakId(), prosessTaskData.getAktørId(), behandlingId);
        log.info("Utført for behandling: {}", behandlingId);
    }
}
