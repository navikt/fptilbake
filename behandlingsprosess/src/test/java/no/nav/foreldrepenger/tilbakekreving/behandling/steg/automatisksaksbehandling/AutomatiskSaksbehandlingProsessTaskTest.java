package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.AvsluttBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.SendVedtaksbrevTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.SendØkonomiTibakekerevingsVedtakTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.CdiDbAwareTest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@CdiDbAwareTest
public class AutomatiskSaksbehandlingProsessTaskTest {

    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private TotrinnRepository totrinnRepository;
    @Inject
    private ProsessTaskTjeneste taskTjeneste;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private AutomatiskSaksbehandlingProsessTask automatiskSaksbehandlingProsessTask;
    ScenarioSimple scenarioSimple = ScenarioSimple.simple();
    private Behandling behandling;
    private Long behandlingId;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        totrinnRepository = new TotrinnRepository(entityManager);


        BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager,
                new BehandlingModellRepository(), behandlingskontrollEventPublisererMock));

        automatiskSaksbehandlingProsessTask = new AutomatiskSaksbehandlingProsessTask(behandlingRepository,
                behandlingskontrollTjeneste);
        entityManager.setFlushMode(FlushModeType.AUTO);
        behandling = scenarioSimple.medBehandlingType(BehandlingType.TILBAKEKREVING)
                .medDefaultKravgrunnlag()
                .lagre(repositoryProvider);
        behandlingId = behandling.getId();
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING,
                BehandlingStegStatus.UTGANG, BehandlingStegStatus.UTGANG);

    }

    @Test
    public void skal_saksbehandle_automatisk() {
        int antallKlareFør = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR).size();
        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.isAutomatiskSaksbehandlet()).isTrue();
        assertThat(behandling.getAnsvarligSaksbehandler()).isEqualTo("VL");
        assertThat(behandling.getAnsvarligBeslutter()).isEqualTo("VL");
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.IVERKSETTER_VEDTAK);

        var prosessTasker = taskTjeneste.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker.size() - antallKlareFør).isEqualTo(3);
        List<TaskType> prosessTaskNavn = prosessTasker.stream()
                .map(ProsessTaskData::taskType)
                .collect(Collectors.toList());
        assertThat(prosessTaskNavn.contains(TaskType.forProsessTask(SendVedtaksbrevTask.class))).isFalse();
        assertThat(prosessTaskNavn.contains(TaskType.forProsessTask(AvsluttBehandlingTask.class))).isTrue();
        assertThat(prosessTaskNavn.contains(TaskType.forProsessTask(SendØkonomiTibakekerevingsVedtakTask.class))).isTrue();
        assertThat(prosessTaskNavn.contains(TaskType.forProsessTask(SendVedtakHendelserTilDvhTask.class))).isTrue();

        Optional<FaktaFeilutbetaling> faktaFeilutbetalingData = repositoryProvider.getFaktaFeilutbetalingRepository()
                .finnFaktaOmFeilutbetaling(behandlingId);
        assertThat(faktaFeilutbetalingData).isPresent();
        FaktaFeilutbetaling faktaFeilutbetaling = faktaFeilutbetalingData.get();
        assertThat(faktaFeilutbetaling.getBegrunnelse()).isEqualTo(
                AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
        assertThat(faktaFeilutbetaling.getFeilutbetaltPerioder()
                .stream()
                .allMatch(faktaFeilutbetalingPeriode -> HendelseType.FP_ANNET_HENDELSE_TYPE.equals(
                        faktaFeilutbetalingPeriode.getHendelseType()))).isTrue();
        assertThat(faktaFeilutbetaling.getFeilutbetaltPerioder()
                .stream()
                .allMatch(faktaFeilutbetalingPeriode -> HendelseUnderType.ANNET_FRITEKST.equals(
                        faktaFeilutbetalingPeriode.getHendelseUndertype()))).isTrue();

        Optional<VurdertForeldelse> vurdertForeldelseData = repositoryProvider.getVurdertForeldelseRepository()
                .finnVurdertForeldelse(behandlingId);
        assertThat(vurdertForeldelseData).isPresent();
        VurdertForeldelse vurdertForeldelse = vurdertForeldelseData.get();
        assertThat(vurdertForeldelse.getVurdertForeldelsePerioder()
                .stream()
                .allMatch(vurdertForeldelsePeriode -> ForeldelseVurderingType.IKKE_FORELDET.equals(
                        vurdertForeldelsePeriode.getForeldelseVurderingType()))).isTrue();
        assertThat(vurdertForeldelse.getVurdertForeldelsePerioder()
                .stream()
                .allMatch(
                        vurdertForeldelsePeriode -> AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(
                                vurdertForeldelsePeriode.getBegrunnelse()))).isTrue();

        Optional<VilkårVurderingEntitet> vilkårsVurderingData = repositoryProvider.getVilkårsvurderingRepository()
                .finnVilkårsvurdering(behandlingId);
        assertThat(vilkårsVurderingData).isPresent();
        VilkårVurderingEntitet vilkårVurderingEntitet = vilkårsVurderingData.get();
        assertThat(vilkårVurderingEntitet.getPerioder()
                .stream()
                .allMatch(periode -> VilkårResultat.FORSTO_BURDE_FORSTÅTT.equals(periode.getVilkårResultat()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder()
                .stream()
                .allMatch(periode -> Aktsomhet.SIMPEL_UAKTSOM.equals(periode.getAktsomhetResultat()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder()
                .stream()
                .allMatch(periode -> AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(
                        periode.getBegrunnelseAktsomhet()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder()
                .stream()
                .allMatch(periode -> AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(
                        periode.getBegrunnelse()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder()
                .stream()
                .allMatch(VilkårVurderingPeriodeEntitet::tilbakekrevesSmåbeløp)).isFalse();

        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository()
                .hentHistorikk(behandlingId);
        assertThat(historikkinnslager.stream()
                .allMatch(
                        historikkinnslag -> HistorikkAktør.VEDTAKSLØSNINGEN.equals(historikkinnslag.getAktør()))).isTrue();

        assertThat(totrinnRepository.hentTotrinngrunnlag(behandling)).isEmpty();
    }

    @Test
    public void skal_ikke_saksbehandle_automatisk_når_behandling_er_på_vent() {
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling,
                AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.FAKTA_FEILUTBETALING,
                LocalDateTime.now().plusDays(2), Venteårsak.AVVENTER_DOKUMENTASJON);
        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        assertThat(behandling.isAutomatiskSaksbehandlet()).isFalse();
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.UTREDES);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING);
    }

    @Test
    public void skal_ikke_saksbehandle_automatisk_når_behandling_er_allerede_avsluttet() {
        behandling.avsluttBehandling();
        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        assertThat(behandling.isAutomatiskSaksbehandlet()).isFalse();
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.AVSLUTTET);
    }

    @Test
    public void skal_ikke_saksbehandle_automatisk_når_behandling_er_allerede_saksbehandlet() {
        behandling.setAnsvarligSaksbehandler("1234");
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        assertThat(behandling.isAutomatiskSaksbehandlet()).isFalse();
        assertThat(behandling.getAnsvarligSaksbehandler()).isNotEqualTo("VL");
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.UTREDES);
    }

    private ProsessTaskData lagProsesTaskData() {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(AutomatiskSaksbehandlingProsessTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());
        return prosessTaskData;
    }

}
