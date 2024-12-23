package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.henleggelse.SendHenleggelsesbrevTask;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ApplicationScoped
public class HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BrevSporingRepository brevSporingRepository;
    private HistorikkinnslagRepository historikkRepository;

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private static final long OPPRETTELSE_DAGER_BEGRENSNING = 6L;

    HenleggBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     ProsessTaskTjeneste taskTjeneste,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.brevSporingRepository = repositoryProvider.getBrevSporingRepository();
        this.historikkRepository = repositoryProvider.getHistorikkinnslagRepository();
        this.taskTjeneste = taskTjeneste;

        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
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
        henleggelseBrevTask.setProperty(TaskProperties.BESTILLING_UUID, UUID.randomUUID().toString()); // Brukes som eksternReferanseId ved journalføring av brev
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
        var aktør = BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT.equals(årsakKode) ? HistorikkAktør.VEDTAKSLØSNINGEN : HistorikkAktør.SAKSBEHANDLER;
        var historikkinnslag = opprettHistorikkinnslagForHenleggelse(behandling, årsakKode, begrunnelse, aktør);
        historikkRepository.lagre(historikkinnslag);
    }

    public static Historikkinnslag opprettHistorikkinnslagForHenleggelse(Behandling behandling, BehandlingResultatType årsakKode, String begrunnelse, HistorikkAktør aktør) {
        return new Historikkinnslag.Builder()
            .medAktør(aktør)
            .medBehandlingId(behandling.getId())
            .medFagsakId(behandling.getFagsakId())
            .medTittel("Behandling er henlagt")
            .addLinje(årsakKode.getNavn())
            .addLinje(begrunnelse)
            .build();
    }
}
