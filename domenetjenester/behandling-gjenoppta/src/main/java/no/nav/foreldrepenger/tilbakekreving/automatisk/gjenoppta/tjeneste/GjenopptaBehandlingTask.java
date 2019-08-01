package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(GjenopptaBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class GjenopptaBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "behandlingskontroll.gjenopptaBehandling";

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    public GjenopptaBehandlingTask() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingTask(BehandlingRepository behandlingRepository,
                                   BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingsId = prosessTaskData.getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingsId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);

        behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, kontekst);
        behandlingskontrollTjeneste.behandlingFramføringTilSenereBehandlingSteg(kontekst, BehandlingStegType.TBKGSTEG);

        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }
}
