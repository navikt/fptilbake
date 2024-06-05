package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "saksbehandling.henleggbehandling", prioritet = 3)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HenleggBehandlingTask implements ProsessTaskHandler {

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    public HenleggBehandlingTask() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTask(HenleggBehandlingTjeneste henleggBehandlingTjeneste) {
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();

        henleggBehandlingTjeneste.henleggBehandling(behandlingId, BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);
    }
}
