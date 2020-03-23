package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private BrevSporingRepository brevSporingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private static final String HENLEGGELSESBREV_TASK_TYPE = "brev.sendhenleggelse";
    private static final String SELVBETJENING_HENLAGT_TASKTYPE = "send.beskjed.tilbakekreving.henlagt.selvbetjening";

    HenleggBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     ProsessTaskRepository prosessTaskRepository,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.prosessTaskRepository = prosessTaskRepository;

        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void henleggBehandlingManuelt(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            throw BehandlingFeil.FACTORY.kanIkkeHenleggeBehandling(behandlingId).toException();
        }
        doHenleggBehandling(behandlingId, årsakKode, begrunnelse);
    }

    public void henleggBehandling(long behandlingId, BehandlingResultatType årsakKode) {
        doHenleggBehandling(behandlingId, årsakKode, null);
    }

    private void doHenleggBehandling(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (erDetSendtVarsel(behandlingId)) {
            opprettHenleggelseTasker(behandling);
        }
        opprettHistorikkinnslag(behandling, årsakKode, begrunnelse);
        eksternBehandlingRepository.deaktivateTilkobling(behandlingId);
    }

    private void opprettHenleggelseTasker(Behandling behandling) {
        ProsessTaskData henleggelseBrevTask = new ProsessTaskData(HENLEGGELSESBREV_TASK_TYPE);
        henleggelseBrevTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        henleggelseBrevTask.setCallIdFraEksisterende();

        ProsessTaskData selvbetjeningTask = new ProsessTaskData(SELVBETJENING_HENLAGT_TASKTYPE);
        selvbetjeningTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        selvbetjeningTask.setCallIdFraEksisterende();

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.addNesteParallell(henleggelseBrevTask);
        taskGruppe.addNesteParallell(selvbetjeningTask);

        prosessTaskRepository.lagre(taskGruppe);
    }

    private boolean erDetSendtVarsel(long behandlingId) {
        return brevSporingRepository.harVarselBrevSendtForBehandlingId(behandlingId);
    }

    private void opprettHistorikkinnslag(Behandling behandling, BehandlingResultatType årsakKode, String begrunnelse) {
        if (BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT.equals(årsakKode)) {
            historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.VEDTAKSLØSNINGEN);
        } else {
            historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.SAKSBEHANDLER);
        }

    }
}
