package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(TvingHenlegglBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class TvingHenlegglBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "behandlingskontroll.tvingHenleggBehandling";

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    TvingHenlegglBehandlingTask(){
        // for CDI proxy
    }

    @Inject
    public TvingHenlegglBehandlingTask(BehandlingRepository behandlingRepository,
                                       BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                       HistorikkinnslagTjeneste historikkinnslagTjeneste){
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingsId = prosessTaskData.getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingsId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
            behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, true);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD);

        opprettHistorikkinnslagForTvingHenleggelse(behandling);
    }

    private void opprettHistorikkinnslagForTvingHenleggelse(Behandling behandling) {
        historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, BehandlingResultatType.HENLAGT_TEKNISK_VEDLIKEHOLD, null, HistorikkAktør.VEDTAKSLØSNINGEN);
    }
}
