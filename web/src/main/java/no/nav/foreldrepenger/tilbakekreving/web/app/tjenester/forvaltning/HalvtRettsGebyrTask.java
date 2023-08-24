package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskSaksbehandlingVurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("kravgrunnlag.sjekk.halvt.gebyr")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HalvtRettsGebyrTask implements ProsessTaskHandler {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingRepository behandlingRepository;

    private AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste;

    HalvtRettsGebyrTask() {
        // for CDI
    }

    @Inject
    public HalvtRettsGebyrTask(BehandlingRepositoryProvider repositoryProvider,
                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                               AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.halvtRettsgebyrTjeneste = halvtRettsgebyrTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (halvtRettsgebyrTjeneste.lavFeilutbetalingKanVentePåAutomatiskBehandling(behandlingId)) {
            var fristDato = halvtRettsgebyrTjeneste.ventefristForTilfelleSomKanAutomatiskSaksbehandles(behandlingId);
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                BehandlingStegType.TBKGSTEG, fristDato, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);

        }
    }


}
