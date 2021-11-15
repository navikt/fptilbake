package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste.HendelseHåndtererTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "hendelser.håndterVedtakFattet", maxFailedRuns = 5)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class HåndterVedtakFattetTask implements ProsessTaskHandler {

    private HendelseHåndtererTjeneste hendelseHåndterer;

    HåndterVedtakFattetTask() {
        // CDI
    }

    @Inject
    public HåndterVedtakFattetTask(HendelseHåndtererTjeneste hendelseHåndterer) {
        this.hendelseHåndterer = hendelseHåndterer;
    }

    @Override
    public void doTask(ProsessTaskData taskData) {
        HendelseTaskDataWrapper dataWrapper = new HendelseTaskDataWrapper(taskData);
        dataWrapper.validerTaskDataHåndterVedtakFattet();

        var henvisning = hendelseHåndterer.hentHenvisning(UUID.fromString(dataWrapper.getBehandlingUuid()));
        hendelseHåndterer.håndterHendelse(dataWrapper, henvisning);
    }
}
