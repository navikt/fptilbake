package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.SendHenleggelsesbrevTask;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ApplicationScoped
public class HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private BrevSporingRepository brevSporingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private static final long OPPRETTELSE_DAGER_BEGRENSNING = 6L;

    HenleggBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     ProsessTaskTjeneste taskTjeneste,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.taskTjeneste = taskTjeneste;

        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public boolean kanHenleggeBehandlingManuelt(Behandling behandling) {
        if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType())) {
            return true;
        } else
            return !erBehandlingOpprettetAutomatiskEtterBestemteDager(behandling) && !grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId());
    }

    private boolean erBehandlingOpprettetAutomatiskEtterBestemteDager(Behandling behandling) {
        return !behandling.isManueltOpprettet() && behandling.getOpprettetTidspunkt().isAfter(
                LocalDate.now().atStartOfDay().minusDays(OPPRETTELSE_DAGER_BEGRENSNING));
    }

    private boolean erBehandlingOpprettetAutomatiskFørBestemteDager(Behandling behandling) {
        return !behandling.isManueltOpprettet() && behandling.getOpprettetTidspunkt().isBefore(
                LocalDate.now().atStartOfDay().minusDays(OPPRETTELSE_DAGER_BEGRENSNING));
    }

    public void henleggBehandlingManuelt(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse, String fritekst) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (BehandlingType.TILBAKEKREVING.equals(behandling.getType()) &&
                (grunnlagRepository.harGrunnlagForBehandlingId(behandling.getId()) &&
                        !erBehandlingOpprettetAutomatiskFørBestemteDager(behandling))) {
            throw BehandlingFeil.kanIkkeHenleggeBehandling(behandlingId);
        }
        doHenleggBehandling(behandlingId, årsakKode, begrunnelse, fritekst);
    }

    public void henleggBehandling(long behandlingId, BehandlingResultatType årsakKode) {
        doHenleggBehandling(behandlingId, årsakKode, null, null);
    }

    private void doHenleggBehandling(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse, String fritekst) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtførtForHenleggelse(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (kanSendeHenleggeslsesBrev(behandling, årsakKode)) {
            sendHenleggelsesbrev(behandling, fritekst);
        }

        opprettHistorikkinnslag(behandling, årsakKode, begrunnelse);
        eksternBehandlingRepository.deaktivateTilkobling(behandlingId);
    }

    private void sendHenleggelsesbrev(Behandling behandling, String fritekst) {
        var henleggelseBrevTask = ProsessTaskData.forTaskType(TaskType.forProsessTask(SendHenleggelsesbrevTask.class));
        henleggelseBrevTask.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        henleggelseBrevTask.setPayload(fritekst);
        henleggelseBrevTask.setProperty(SendHenleggelsesbrevTask.BESTILLING_UUID, UUID.randomUUID().toString()); // Brukes som eksternReferanseId ved journalføring av brev
        henleggelseBrevTask.setCallIdFraEksisterende();
        taskTjeneste.lagre(henleggelseBrevTask);
    }

    private boolean kanSendeHenleggeslsesBrev(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        if (BehandlingType.TILBAKEKREVING.equals(behandling.getType())) {
            return varselSendt(behandling.getId());
        } else if (BehandlingType.REVURDERING_TILBAKEKREVING.equals(behandling.getType())) {
            return BehandlingResultatType.HENLAGT_FEILOPPRETTET_MED_BREV.equals(behandlingResultatType);
        }
        return false;
    }

    private boolean varselSendt(long behandlingId) {
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
