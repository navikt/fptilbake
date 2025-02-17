package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste.opprettHistorikkinnslagForHenleggelse;

@ApplicationScoped
@ProsessTask(value = "behandlingskontroll.tvingHenleggBehandling", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class TvingHenlegglBehandlingTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private HistorikkinnslagRepository historikkRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    TvingHenlegglBehandlingTask() {
        // for CDI proxy
    }

    @Inject
    public TvingHenlegglBehandlingTask(BehandlingRepositoryProvider repositoryProvider,
                                       BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.historikkRepository = repositoryProvider.getHistorikkinnslagRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD);
        opprettHistorikkinnslagForTvingHenleggelse(behandling);
        eksternBehandlingRepository.deaktivateTilkobling(behandlingId);
    }

    private void opprettHistorikkinnslagForTvingHenleggelse(Behandling behandling) {
        var historikkinnslag = opprettHistorikkinnslagForHenleggelse(behandling, BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD, null, HistorikkAktør.VEDTAKSLØSNINGEN);
        historikkRepository.lagre(historikkinnslag);
    }
}
