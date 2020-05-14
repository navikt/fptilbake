package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.exception.TekniskException;

public class VergeRestTjenesteTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private VergeTjeneste vergeTjenesteMock = mock(VergeTjeneste.class);
    private BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private VergeRestTjeneste vergeRestTjeneste = new VergeRestTjeneste(behandlingTjenesteMock, vergeTjenesteMock);

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
        Behandling behandling = scenario.lagre(new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager()));
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763493", TekniskException.class, () -> vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void kan_ikke_opprette_verge_når_behandling_har_allerede_verge_aksjonspunkt() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_FEILUTBETALING);
        Behandling behandling = scenario.lagre(new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager()));
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-185321", TekniskException.class, () -> vergeRestTjeneste.opprettVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void skal_opprette_verge_når_behandling_er_i_fakta_steg() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        Behandling behandling = scenario.lagre(new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager()));
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
        Behandling behandling = scenario.lagre(new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager()));
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        assertThrows("FPT-763494", TekniskException.class, () -> vergeRestTjeneste.fjernVerge(new BehandlingIdDto(behandling.getId())));
    }

    @Test
    public void skal_fjerne_verge() {
        ScenarioSimple scenario = ScenarioSimple.simple();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_VERGE);
        Behandling behandling = scenario.lagre(new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager()));
        when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
        vergeRestTjeneste.fjernVerge(new BehandlingIdDto(behandling.getId()));
        verify(vergeTjenesteMock, atLeastOnce()).fjernVergeGrunnlagOgAksjonspunkt(any());
    }
}
