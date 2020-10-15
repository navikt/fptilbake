package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatisksaksbehandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.VurderForeldelseAksjonspunktUtleder;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.AutomatiskVurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurderForeldelseAksjonspunktUtlederImpl;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.AutomatiskVurdertVilkårTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingHistorikkInnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktafeilutbetaling.FaktaFeilutbetalingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktaverge.FaktaVergeSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.fattevedtak.FatteVedtakSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.foreslåvedtak.ForeslåVedtakSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TestStegKonfig;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.MottattGrunnlagSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.AvsluttBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.ProsessTaskIverksett;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.SendVedtaksbrevTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak.SendØkonomiTibakekerevingsVedtakTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurderforeldelse.VurderForeldelseSteg;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurdervilkår.VurderTilbakekrevingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles.FellesQueriesForBehandlingRepositories;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendVedtakHendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AutomatiskFaktaFastsettelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.AvklartFaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagKonverter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjenesteImpl;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class AutomatiskSaksbehandlingProsessTaskTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProvider(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final KravgrunnlagRepository kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
    private final AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    private final TotrinnRepository totrinnRepository = new TotrinnRepository(repositoryRule.getEntityManager());
    private final VarselresponsRepository varselresponsRepository = new VarselresponsRepository(repositoryRule.getEntityManager());
    private final ProsessTaskRepository taskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private final FellesQueriesForBehandlingRepositories fellesQueriesForBehandlingRepositories = new FellesQueriesForBehandlingRepositories(repositoryRule.getEntityManager());
    private final BehandlingVenterRepository behandlingVenterRepository = new BehandlingVenterRepository(fellesQueriesForBehandlingRepositories);
    private final BehandlingKandidaterRepository behandlingKandidaterRepository = new BehandlingKandidaterRepository(fellesQueriesForBehandlingRepositories);
    private final InternalManipulerBehandling manipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
    private final HistorikkInnslagKonverter historikkInnslagKonverter = new HistorikkInnslagKonverter(aksjonspunktRepository);
    private final HistorikkTjenesteAdapter historikkTjenesteAdapter = new HistorikkTjenesteAdapter(repositoryProvider.getHistorikkRepository(), historikkInnslagKonverter);
    private final AvklartFaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = new AvklartFaktaFeilutbetalingTjeneste(repositoryProvider.getFaktaFeilutbetalingRepository(),
        historikkTjenesteAdapter);
    private final VarselresponsTjeneste varselresponsTjeneste = new VarselresponsTjenesteImpl(varselresponsRepository);
    private final GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste = new GjenopptaBehandlingTjenesteImpl(taskRepository, behandlingKandidaterRepository,
        behandlingVenterRepository, repositoryProvider, varselresponsTjeneste);
    private final KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider, gjenopptaBehandlingTjeneste,
        null, null);
    private final AutomatiskFaktaFastsettelseTjeneste faktaFastsettelseTjeneste = new AutomatiskFaktaFastsettelseTjeneste(faktaFeilutbetalingTjeneste,kravgrunnlagTjeneste);
    private final VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder = new VurderForeldelseAksjonspunktUtlederImpl(Period.ofWeeks(-1),
        kravgrunnlagRepository, behandlingRepository);
    private final KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste = new KravgrunnlagBeregningTjeneste(kravgrunnlagRepository);
    private final VurdertForeldelseTjeneste vurdertForeldelseTjeneste = new VurdertForeldelseTjeneste(repositoryProvider, historikkTjenesteAdapter, kravgrunnlagBeregningTjeneste);
    private final AutomatiskVurdertForeldelseTjeneste automatiskVurdertForeldelseTjeneste = new AutomatiskVurdertForeldelseTjeneste(vurderForeldelseAksjonspunktUtleder,vurdertForeldelseTjeneste);
    private final VilkårsvurderingHistorikkInnslagTjeneste vilkårsvurderingHistorikkInnslagTjeneste = new VilkårsvurderingHistorikkInnslagTjeneste(historikkTjenesteAdapter,
        repositoryProvider);
    private final VilkårsvurderingTjeneste vilkårsvurderingTjeneste = new VilkårsvurderingTjeneste(vurdertForeldelseTjeneste, repositoryProvider,
        vilkårsvurderingHistorikkInnslagTjeneste, kravgrunnlagBeregningTjeneste);
    private final AutomatiskVurdertVilkårTjeneste automatiskVurdertVilkårTjeneste = new AutomatiskVurdertVilkårTjeneste(vilkårsvurderingTjeneste, aksjonspunktRepository);
    private final TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste = new TilbakekrevingBeregningTjeneste(repositoryProvider, kravgrunnlagBeregningTjeneste);
    private final ProsessTaskIverksett prosessTaskIverksett = new ProsessTaskIverksett(taskRepository, repositoryProvider.getBrevSporingRepository());

    private final BehandlingModellRepository behandlingModellRepositoryMock = Mockito.mock(BehandlingModellRepository.class);
    private final BehandlingskontrollEventPubliserer behandlingskontrollEventPublisererMock = mock(BehandlingskontrollEventPubliserer.class);
    private final BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepositoryMock,
        behandlingskontrollEventPublisererMock);
    private final AutomatiskSaksbehandlingProsessTask automatiskSaksbehandlingProsessTask = new AutomatiskSaksbehandlingProsessTask(behandlingRepository, behandlingskontrollTjeneste);
    ScenarioSimple scenarioSimple = ScenarioSimple.simple();
    private Behandling behandling;
    private Long behandlingId;

    @Before
    public void setup() {
        repositoryRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        behandling = scenarioSimple.medBehandlingType(BehandlingType.TILBAKEKREVING).medDefaultKravgrunnlag().lagre(repositoryProvider);
        behandlingId = behandling.getId();
        manipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingStegStatus.UTGANG);

        when(behandlingModellRepositoryMock.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        when(behandlingModellRepositoryMock.getModell(any(BehandlingType.class))).thenReturn(lagDummyBehandlingsModell());
    }

    @Test
    public void skal_saksbehandle_automatisk() {
        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.isAutomatiskSaksbehandlet()).isTrue();
        assertThat(behandling.getAnsvarligSaksbehandler()).isEqualTo("VL");
        assertThat(behandling.getAnsvarligBeslutter()).isEqualTo("VL");
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.IVERKSETTER_VEDTAK);

        List<ProsessTaskData> prosessTasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTasker.size()).isEqualTo(3);
        List<String> prosessTaskNavn = prosessTasker.stream().map(ProsessTaskData::getTaskType).collect(Collectors.toList());
        assertThat(prosessTaskNavn.contains(SendVedtaksbrevTask.TASKTYPE)).isFalse();
        assertThat(prosessTaskNavn.contains(AvsluttBehandlingTask.TASKTYPE)).isTrue();
        assertThat(prosessTaskNavn.contains(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE)).isTrue();
        assertThat(prosessTaskNavn.contains(SendVedtakHendelserTilDvhTask.TASKTYPE)).isTrue();

        Optional<FaktaFeilutbetaling> faktaFeilutbetalingData = repositoryProvider.getFaktaFeilutbetalingRepository().finnFaktaOmFeilutbetaling(behandlingId);
        assertThat(faktaFeilutbetalingData).isPresent();
        FaktaFeilutbetaling faktaFeilutbetaling = faktaFeilutbetalingData.get();
        assertThat(faktaFeilutbetaling.getBegrunnelse()).isEqualTo(AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE);
        assertThat(faktaFeilutbetaling.getFeilutbetaltPerioder().stream()
            .allMatch(faktaFeilutbetalingPeriode ->
                HendelseType.FP_ANNET_HENDELSE_TYPE.equals(faktaFeilutbetalingPeriode.getHendelseType()))).isTrue();
        assertThat(faktaFeilutbetaling.getFeilutbetaltPerioder().stream()
            .allMatch(faktaFeilutbetalingPeriode ->HendelseUnderType.ANNET_FRITEKST.equals(faktaFeilutbetalingPeriode.getHendelseUndertype()))).isTrue();

        Optional<VurdertForeldelse> vurdertForeldelseData = repositoryProvider.getVurdertForeldelseRepository().finnVurdertForeldelse(behandlingId);
        assertThat(vurdertForeldelseData).isPresent();
        VurdertForeldelse vurdertForeldelse = vurdertForeldelseData.get();
        assertThat(vurdertForeldelse.getVurdertForeldelsePerioder().stream()
            .allMatch(vurdertForeldelsePeriode -> ForeldelseVurderingType.IKKE_FORELDET.equals(vurdertForeldelsePeriode.getForeldelseVurderingType()))).isTrue();
        assertThat(vurdertForeldelse.getVurdertForeldelsePerioder().stream()
            .allMatch(vurdertForeldelsePeriode -> AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(vurdertForeldelsePeriode.getBegrunnelse()))).isTrue();

        Optional<VilkårVurderingEntitet> vilkårsVurderingData = repositoryProvider.getVilkårsvurderingRepository().finnVilkårsvurdering(behandlingId);
        assertThat(vilkårsVurderingData).isPresent();
        VilkårVurderingEntitet vilkårVurderingEntitet = vilkårsVurderingData.get();
        assertThat(vilkårVurderingEntitet.getPerioder().stream().allMatch(periode -> VilkårResultat.FORSTO_BURDE_FORSTÅTT.equals(periode.getVilkårResultat()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder().stream().allMatch(periode -> Aktsomhet.SIMPEL_UAKTSOM.equals(periode.getAktsomhetResultat()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder().stream().allMatch(periode ->
            AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(periode.getBegrunnelseAktsomhet()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder().stream().allMatch(periode ->
            AutomatiskSaksbehandlingTaskProperties.AUTOMATISK_SAKSBEHANDLING_BEGUNNLESE.equals(periode.getBegrunnelse()))).isTrue();
        assertThat(vilkårVurderingEntitet.getPerioder().stream().allMatch(VilkårVurderingPeriodeEntitet::tilbakekrevesSmåbeløp)).isFalse();

        List<Historikkinnslag> historikkinnslager = repositoryProvider.getHistorikkRepository().hentHistorikk(behandlingId);
        assertThat(historikkinnslager.stream().allMatch(historikkinnslag -> HistorikkAktør.VEDTAKSLØSNINGEN.equals(historikkinnslag.getAktør()))).isTrue();

        assertThat(totrinnRepository.hentTotrinngrunnlag(behandling)).isEmpty();
    }

    @Test
    public void skal_ikke_saksbehandle_automatisk_når_behandling_er_på_vent() {
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
            BehandlingStegType.FAKTA_FEILUTBETALING, LocalDateTime.now().plusDays(2), Venteårsak.AVVENTER_DOKUMENTASJON);
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
        behandlingRepository.lagre(behandling,behandlingLås);

        automatiskSaksbehandlingProsessTask.doTask(lagProsesTaskData());
        assertThat(behandling.isAutomatiskSaksbehandlet()).isFalse();
        assertThat(behandling.getAnsvarligSaksbehandler()).isNotEqualTo("VL");
        assertThat(behandling.getStatus()).isEqualByComparingTo(BehandlingStatus.UTREDES);
    }

    private ProsessTaskData lagProsesTaskData() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(AutomatiskSaksbehandlingProsessTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandlingId, behandling.getAktørId().getId());
        return prosessTaskData;
    }

    private BehandlingModell lagDummyBehandlingsModell() {
        List<TestStegKonfig> steg = Lists.newArrayList(
            new TestStegKonfig(BehandlingStegType.FAKTA_VERGE, BehandlingType.TILBAKEKREVING, new FaktaVergeSteg(behandlingRepository)),
            new TestStegKonfig(BehandlingStegType.TBKGSTEG, BehandlingType.TILBAKEKREVING, new MottattGrunnlagSteg(behandlingRepository, behandlingskontrollTjeneste,
                gjenopptaBehandlingTjeneste, null, Period.ofWeeks(-1))),
            new TestStegKonfig(BehandlingStegType.FAKTA_FEILUTBETALING, BehandlingType.TILBAKEKREVING,
                new FaktaFeilutbetalingSteg(behandlingRepository,faktaFastsettelseTjeneste)),
            new TestStegKonfig(BehandlingStegType.FORELDELSEVURDERINGSTEG, BehandlingType.TILBAKEKREVING, new VurderForeldelseSteg(repositoryProvider,
                vurderForeldelseAksjonspunktUtleder, automatiskVurdertForeldelseTjeneste)),
            new TestStegKonfig(BehandlingStegType.VTILBSTEG, BehandlingType.TILBAKEKREVING,
                new VurderTilbakekrevingSteg(repositoryProvider, automatiskVurdertVilkårTjeneste)),
            new TestStegKonfig(BehandlingStegType.FORESLÅ_VEDTAK, BehandlingType.TILBAKEKREVING, new ForeslåVedtakSteg(behandlingRepository)),
            new TestStegKonfig(BehandlingStegType.FATTE_VEDTAK, BehandlingType.TILBAKEKREVING, new FatteVedtakSteg(repositoryProvider, totrinnRepository,
                tilbakekrevingBeregningTjeneste, historikkTjenesteAdapter)),
            new TestStegKonfig(BehandlingStegType.IVERKSETT_VEDTAK, BehandlingType.TILBAKEKREVING, new IverksetteVedtakSteg(repositoryProvider, prosessTaskIverksett)));

        BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> finnSteg = map(steg);
        BehandlingModellImpl modell = new BehandlingModellImpl(BehandlingType.TILBAKEKREVING, finnSteg);

        steg.forEach(konfig -> modell.leggTil(konfig.getBehandlingStegType(), konfig.getBehandlingType()));
        return modell;
    }

    private static BehandlingModellImpl.TriFunction<BehandlingStegType, BehandlingType, BehandlingSteg> map(List<TestStegKonfig> input) {
        Map<List<?>, BehandlingSteg> resolver = new HashMap<>();
        for (TestStegKonfig konfig : input) {
            List<?> key = Arrays.asList(konfig.getBehandlingStegType(), konfig.getBehandlingType());
            resolver.put(key, konfig.getSteg());
        }
        return (t, u) -> resolver.get(Arrays.asList(t, u));
    }

}
