package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.IVERKSETT_VEDTAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class HenleggBehandlingTjenesteTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Mock
    private HistorikkRepository historikkRepositoryMock;

    @Mock
    private BehandlingModellRepository behandlingModellRepository;

    @Mock
    private EksternBehandlingRepository eksternBehandlingRepositoryMock;

    @Mock
    private BehandlingStegType behandlingStegType;

    @Mock
    private BehandlingModell modell;

    @Mock
    private BehandlingresultatRepository behandlingresultatRepository;

    @Mock
    private KravgrunnlagRepository kravgrunnlagRepositoryMock;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    @Inject
    private KodeverkRepository kodeverkRepository;

    private ProsessTaskRepository prosessTaskRepository;

    @Mock
    private VarselRepository varselRepository;

    @Inject
    @KonfigVerdi(value = "bruker.gruppenavn.saksbehandler")
    private String gruppenavnSaksbehandler;

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    private Behandling behandling;

    @Before
    public void setUp() {
        System.setProperty("systembruker.username", "brukerident");

        ScenarioSimple scenario = ScenarioSimple.simple();
        behandling = scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        when(repositoryProvider.getAksjonspunktRepository()).thenReturn(aksjonspunktRepository);
        when(repositoryProvider.getKodeverkRepository()).thenReturn(kodeverkRepository);
        when(repositoryProvider.getHistorikkRepository()).thenReturn(historikkRepositoryMock);
        when(repositoryProvider.getBehandlingresultatRepository()).thenReturn(behandlingresultatRepository);
        when(repositoryProvider.getGrunnlagRepository()).thenReturn(kravgrunnlagRepositoryMock);
        when(repositoryProvider.getEksternBehandlingRepository()).thenReturn(eksternBehandlingRepositoryMock);
        when(repositoryProvider.getBehandlingRepository().finnBehandlingStegType(IVERKSETT_VEDTAK.getKode())).thenReturn(behandlingStegType);
        when(repositoryProvider.getVarselRepository()).thenReturn(varselRepository);
        BehandlingskontrollTjenesteImpl behandlingskontrollTjenesteImpl = new BehandlingskontrollTjenesteImpl(repositoryProvider,
            behandlingModellRepository, null);
        when(behandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        when(behandlingModellRepository.getModell(any())).thenReturn(modell);
        when(modell.erStegAFørStegB(any(), any())).thenReturn(true);

        prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null, null);

        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepositoryMock, mock(JournalTjeneste.class), mock(PersoninfoAdapter.class));

        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repositoryProvider, prosessTaskRepository, behandlingskontrollTjenesteImpl, historikkinnslagTjeneste);
    }

    @Test
    public void skal_henlegge_behandling_uten_brev() {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProvider.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void skal_henlegge_behandling_med_aksjonspunkt() {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        Aksjonspunkt aksjonspunkt = repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProvider.getBehandlingRepository(), atLeastOnce()).lagre(eq(behandling), any(BehandlingLås.class));
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
    }

    @Test
    public void skal_ikke_henlegge_behandling_manuelt_når_grunnlag_finnes() {
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        when(kravgrunnlagRepositoryMock.harGrunnlagForBehandlingId(behandling.getId())).thenReturn(true);

        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663491");

        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), behandlingsresultat, "");

    }

    @Test
    public void skal_henlegge_behandling_manuelt_når_grunnlag_ikke_finnes() {
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        when(kravgrunnlagRepositoryMock.harGrunnlagForBehandlingId(behandling.getId())).thenReturn(false);

        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), behandlingsresultat, "");

        verify(historikkRepositoryMock, atLeastOnce()).lagre(any(Historikkinnslag.class));
        verify(repositoryProvider.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));

    }

    @Test
    public void kan_henlegge_behandling_som_er_satt_på_vent() {
        // Arrange
        AksjonspunktDefinisjon def = AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING;
        Aksjonspunkt aksjonspunkt = repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, def);
        repositoryProvider.getAksjonspunktRepository().setFrist(aksjonspunkt, LocalDateTime.now(), null);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        verify(historikkRepositoryMock, atLeastOnce()).lagre(any(Historikkinnslag.class));
        verify(repositoryProvider.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void kan_henlegge_behandling_der_vedtak_er_foreslått() {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        verify(historikkRepositoryMock, atLeastOnce()).lagre(any(Historikkinnslag.class));
        verify(repositoryProvider.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void kan_ikke_henlegge_behandling_der_vedtak_er_fattet() {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, IVERKSETT_VEDTAK);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-143308");

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
    }

    @Test
    public void kan_ikke_henlegge_behandling_som_allerede_er_henlagt() {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-143308");

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        // forsøker å henlegge behandlingen igjen
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
    }

    @Test
    public void kan_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isEmpty();
    }

    @Test
    public void kan_sende_henleggelsesbrev_hvis_varselbrev_er_sendt() {
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        Aksjonspunkt aksjonspunkt = repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        VarselInfo varselInfo = VarselInfo.builder()
            .medBehandlingId(behandling.getId())
            .medVarselTekst("hello")
            .medVarselBeløp(1000l).build();

        when(varselRepository.finnVarsel(behandling.getId())).thenReturn(Optional.of(varselInfo));

        repositoryProvider.getAksjonspunktRepository().setTilUtført(aksjonspunkt);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).getTaskType()).isEqualTo("brev.sendhenleggelse");
    }

}
