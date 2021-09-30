package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.selvbetjening.klient.task.SendVedtakFattetTilSelvbetjeningTask;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskTjenesteImpl;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class IverksetteVedtakStegTest {

    private BehandlingRepositoryProvider repoProvider;
    private ProsessTaskTjeneste taskTjeneste;
    private BrevSporingRepository brevSporingRepository;
    private IverksetteVedtakSteg iverksetteVedtakSteg;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BehandlingRepository behandlingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private EntityManager entityManager;

    private final ScenarioSimple simple = ScenarioSimple.simple();
    private Behandling behandling;
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        this.entityManager = entityManager;
        repoProvider = new BehandlingRepositoryProvider(entityManager);
        taskTjeneste = new ProsessTaskTjenesteImpl(new ProsessTaskRepositoryImpl(entityManager, null, Mockito.mock(ProsessTaskEventPubliserer.class)));
        brevSporingRepository = new BrevSporingRepository(entityManager);
        var prosessTaskIverksett = new ProsessTaskIverksett(taskTjeneste,
            brevSporingRepository);
        iverksetteVedtakSteg = new IverksetteVedtakSteg(repoProvider, prosessTaskIverksett);
        behandlingVedtakRepository = repoProvider.getBehandlingVedtakRepository();
        behandlingRepository = repoProvider.getBehandlingRepository();
        eksternBehandlingRepository = repoProvider.getEksternBehandlingRepository();

        entityManager.setFlushMode(FlushModeType.AUTO);
        behandling = simple.lagre(repoProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingskontrollKontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), behandlingLås);
        opprettEksternBehandling(behandling);
    }

    @Test
    public void skal_ikke_utføre_iverksette_vedtak_steg_hvis_vedtak_ikke_finnes() {
        var e= assertThrows(TekniskException.class, () -> iverksetteVedtakSteg.utførSteg(behandlingskontrollKontekst));
        assertThat(e.getMessage()).contains("FPT-131240");
    }

    @Test
    public void skal_utføre_iverksette_vedtak_steg_uten_aksjonpunkter_hvis_behandling_er_allerede_iverksett() {
        opprettBehandlingVedtak(behandling,IverksettingStatus.IVERKSATT);
        BehandleStegResultat stegResultat = iverksetteVedtakSteg.utførSteg(behandlingskontrollKontekst);
        assertThat(stegResultat.getAksjonspunktListe()).isEmpty();
    }

    @Test
    public void skal_utføre_iverksette_vedtak_steg_for_tilbakekreving_behandling() {
        opprettBehandlingVedtak(behandling, IverksettingStatus.IKKE_IVERKSATT);
        lagreInfoOmVarselbrev(behandling.getId(), "jpi1", "did2");
        BehandleStegResultat stegResultat = iverksetteVedtakSteg.utførSteg(behandlingskontrollKontekst);
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertBehandlingVedtak(behandling);

        List<ProsessTaskData> tasker = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker.size()).isEqualTo(5);
        assertThat(tasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class));
        assertThat(tasker.get(1).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtaksbrevTask.class));
        assertThat(tasker.get(2).taskType()).isEqualTo(TaskType.forProsessTask(AvsluttBehandlingTask.class));
        assertThat(tasker.get(3).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtakFattetTilSelvbetjeningTask.class));
        assertThat(tasker.get(4).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtakHendelserTilDvhTask.class));
    }

    @Test
    public void skal_utføre_iverksette_vedtak_steg_uten_å_sende_vedtaksbrev_for_tilbakekreving_revurdering_hvis_behandling_opprettet_for_klage() {
        Behandling revurdering = Behandling.nyBehandlingFor(behandling.getFagsak(), BehandlingType.REVURDERING_TILBAKEKREVING).build();
        List<BehandlingÅrsak> behandlingÅrsaker = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_NFP)
            .medOriginalBehandling(behandling)
            .buildFor(revurdering);
        revurdering.getBehandlingÅrsaker().addAll(behandlingÅrsaker);
        BehandlingLås revurderingBehandlingLås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering,revurderingBehandlingLås);
        opprettEksternBehandling(revurdering);

        opprettBehandlingVedtak(revurdering,IverksettingStatus.IKKE_IVERKSATT);

        BehandleStegResultat stegResultat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(revurdering.getFagsakId(), revurdering.getAktørId(), revurderingBehandlingLås));
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertBehandlingVedtak(revurdering);

        List<ProsessTaskData> tasker = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker.size()).isEqualTo(3);
        assertThat(tasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class));
        assertThat(tasker.get(1).taskType()).isEqualTo(TaskType.forProsessTask(AvsluttBehandlingTask.class));
        assertThat(tasker.get(2).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtakHendelserTilDvhTask.class));
    }

    @Test
    public void skal_utføre_iverksette_vedtak_steg_med_å_sende_vedtaksbrev_for_tilbakekreving_revurdering_hvis_behandling_ikke_opprettet_for_klage() {
        Behandling revurdering = Behandling.nyBehandlingFor(behandling.getFagsak(), BehandlingType.REVURDERING_TILBAKEKREVING).build();
        List<BehandlingÅrsak> behandlingÅrsaker = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_FORELDELSE)
            .medOriginalBehandling(behandling)
            .buildFor(revurdering);
        revurdering.getBehandlingÅrsaker().addAll(behandlingÅrsaker);
        BehandlingLås revurderingBehandlingLås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering,revurderingBehandlingLås);
        opprettEksternBehandling(revurdering);
        opprettBehandlingVedtak(revurdering,IverksettingStatus.IKKE_IVERKSATT);

        BehandleStegResultat stegResultat = iverksetteVedtakSteg.utførSteg(new BehandlingskontrollKontekst(revurdering.getFagsakId(), revurdering.getAktørId(), revurderingBehandlingLås));
        assertThat(stegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
        assertBehandlingVedtak(revurdering);

        List<ProsessTaskData> tasker = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker.size()).isEqualTo(4);
        assertThat(tasker.get(0).taskType()).isEqualTo(TaskType.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class));
        assertThat(tasker.get(1).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtaksbrevTask.class));
        assertThat(tasker.get(2).taskType()).isEqualTo(TaskType.forProsessTask(AvsluttBehandlingTask.class));
        assertThat(tasker.get(3).taskType()).isEqualTo(TaskType.forProsessTask(SendVedtakHendelserTilDvhTask.class));
    }

    private void opprettBehandlingVedtak(Behandling behandling, IverksettingStatus iverksettingStatus) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.FULL_TILBAKEBETALING)
            .medBehandling(behandling).build();
        repoProvider.getBehandlingresultatRepository().lagre(behandlingsresultat);

        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medAnsvarligSaksbehandler("VL")
            .medIverksettingStatus(iverksettingStatus)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(behandlingsresultat).build();
        behandlingVedtakRepository.lagre(behandlingVedtak);
    }

    private void assertBehandlingVedtak(Behandling behandling) {
        Optional<BehandlingVedtak> vedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId());
        assertThat(vedtak).isPresent();
        BehandlingVedtak behandlingVedtak = vedtak.get();
        assertThat(behandlingVedtak.getBehandlingsresultat().getBehandlingResultatType())
            .isEqualByComparingTo(BehandlingResultatType.FULL_TILBAKEBETALING);
        assertThat(behandlingVedtak.getIverksettingStatus()).isEqualByComparingTo(IverksettingStatus.UNDER_IVERKSETTING);
    }

    private void lagreInfoOmVarselbrev(Long behandlingId, String journalpostId, String dokumentId) {
        BrevSporing brevSporing = new BrevSporing.Builder()
            .medBehandlingId(behandlingId)
            .medDokumentId(dokumentId)
            .medJournalpostId(new JournalpostId(journalpostId))
            .medBrevType(BrevType.VARSEL_BREV)
            .build();
        brevSporingRepository.lagre(brevSporing);
        entityManager.flush();
    }

    private void opprettEksternBehandling(Behandling behandling) {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), UUID.randomUUID());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }
}
