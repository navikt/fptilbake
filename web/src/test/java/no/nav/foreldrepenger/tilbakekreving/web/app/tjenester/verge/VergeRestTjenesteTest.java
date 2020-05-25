package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.exception.TekniskException;

public class VergeRestTjenesteTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private VergeTjeneste vergeTjenesteMock = mock(VergeTjeneste.class);
    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private TpsTjeneste tpsTjenesteMock = mock(TpsTjeneste.class);
    private VergeRestTjeneste vergeRestTjeneste = new VergeRestTjeneste(behandlingTjenesteMock, vergeTjenesteMock, tpsTjenesteMock);
    final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    @Test
    public void kan_ikke_opprette_verge_når_behandling_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        behandling.avsluttBehandling();
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763493", TekniskException.class, () -> vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void kan_ikke_opprette_verge_når_behandling_er_på_vent() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763493", TekniskException.class, () -> vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void kan_ikke_opprette_verge_når_behandling_har_allerede_verge_aksjonspunkt() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_FEILUTBETALING);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-185321", TekniskException.class, () -> vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void skal_opprette_verge_når_behandling_er_i_fakta_steg() throws URISyntaxException {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId()));
        verify(vergeTjenesteMock, atLeastOnce()).opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(any());
    }

    @Test
    public void kan_ikke_fjerne_verge_når_behandling_er_avsluttet() {
        Behandling behandling = ScenarioSimple.simple().lagMocked();
        behandling.avsluttBehandling();
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763494", TekniskException.class, () -> vergeRestTjeneste.fjernVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void kan_ikke_fjerne_verge_når_behandling_er_på_vent() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763494", TekniskException.class, () -> vergeRestTjeneste.fjernVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void skal_fjerne_verge() throws URISyntaxException {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_VERGE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        vergeRestTjeneste.fjernVerge(new BehandlingIdDto(behandling.getId()));
        verify(vergeTjenesteMock, atLeastOnce()).fjernVergeGrunnlagOgAksjonspunkt(any());
    }

    @Test
    public void skal_hente_behandlingsmenyvalg_når_behandling_er_aktiv_og_har_ingen_verge() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.empty());
        Response response = vergeRestTjeneste.hentBehandlingsmenyvalg(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(((VergeBehandlingsmenyDto) response.getEntity()).getVergeBehandlingsmeny()).isEqualByComparingTo(VergeBehandlingsmenyEnum.OPPRETT);
    }

    @Test
    public void skal_hente_behandlingsmenyvalg_når_behandling_er_aktiv_og_har_verge() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        VergeEntitet vergeEntitet = VergeEntitet.builder().medVergeAktørId(behandling.getAktørId())
            .medVergeType(VergeType.BARN)
            .medKilde(KildeType.FPTILBAKE.name())
            .medNavn("John Doe")
            .medGyldigPeriode(LocalDate.now().minusMonths(1), LocalDate.now())
            .medBegrunnelse("begunnlese").build();
        when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.of(vergeEntitet));
        Response response = vergeRestTjeneste.hentBehandlingsmenyvalg(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(((VergeBehandlingsmenyDto) response.getEntity()).getVergeBehandlingsmeny()).isEqualByComparingTo(VergeBehandlingsmenyEnum.FJERN);
    }

    @Test
    public void skal_hente_behandlingsmenyvalg_når_behandling_er_på_vent(){
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,BehandlingStegType.TBKGSTEG);
        Behandling behandling = scenario.lagre(repositoryProvider);
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.empty());
        Response response = vergeRestTjeneste.hentBehandlingsmenyvalg(new BehandlingIdDto(behandling.getId()));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(((VergeBehandlingsmenyDto) response.getEntity()).getVergeBehandlingsmeny()).isEqualByComparingTo(VergeBehandlingsmenyEnum.SKJUL);
    }

}
