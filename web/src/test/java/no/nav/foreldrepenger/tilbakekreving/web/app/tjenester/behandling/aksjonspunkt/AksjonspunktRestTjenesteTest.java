package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.time.Period;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste.SimuleringIntegrasjonTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VurderForeldelseDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;

public class AksjonspunktRestTjenesteTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final Long FAGSAK_ID = 12345L;
    private static final Saksnummer SAKSNUMMER = new Saksnummer("12345");
    private static final NavBruker NAV_BRUKER = NavBruker.opprettNy(new AktørId(12345L), Språkkode.nb);
    private static final Period DEFAULT_PERIOD = Period.ofWeeks(4);

    private AksjonspunktApplikasjonTjeneste aksjonspunktTjenesteMock = mock(AksjonspunktApplikasjonTjeneste.class);

    private BehandlingRepository behandlingRepositoryMock = mock(BehandlingRepository.class);
    private TotrinnRepository totrinnRepositoryMock = mock(TotrinnRepository.class);
    private BehandlingRepositoryProvider repositoryProviderMock = mock(BehandlingRepositoryProvider.class);

    private BehandlingskontrollTjeneste behandlingskontrollTjenesteMock = mock(BehandlingskontrollTjeneste.class);
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjenesteMock = mock(BehandlingskontrollAsynkTjeneste.class);
    private BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjenesteMock, behandlingskontrollAsynkTjenesteMock);

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    private Behandling behandling;

    @Before
    public void setup() {
        when(repositoryProviderMock.getBehandlingRepository()).thenReturn(behandlingRepositoryMock);

        BehandlingTjeneste behandlingTjeneste = new BehandlingTjenesteImpl(repositoryProviderMock,
                behandlingskontrollProvider,
                mock(SimuleringIntegrasjonTjeneste.class),
                mock(FagsakTjeneste.class),
                mock(HistorikkinnslagTjeneste.class),
                mock(FpsakKlient.class),
                DEFAULT_PERIOD);

        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(behandlingRepositoryMock,
                totrinnRepositoryMock,
                behandlingTjeneste,
                aksjonspunktTjenesteMock);

        Fagsak fagsak = Fagsak.opprettNy(SAKSNUMMER, NAV_BRUKER);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
    }

    @Test
    public void test_skal_hente_aksjonspunkter() throws URISyntaxException {
        when(behandlingRepositoryMock.hentBehandling(anyLong())).thenReturn(behandling);

        Totrinnsvurdering ttv = Totrinnsvurdering.builder()
                                .medBehandling(behandling)
                                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING).build();
        when(totrinnRepositoryMock.hentTotrinnsvurderinger(any(Behandling.class))).thenReturn(Collections.singleton(ttv));

        Response result = aksjonspunktRestTjeneste.getAksjonspunkter(new BehandlingIdDto("1234"));

        assertThat(result.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(result.getEntity()).isNotNull();
    }

    @Test
    public void test_skal_kalle_bekreftAksjonspunktert() throws URISyntaxException {
        long behandlingId = 12345L;
        Behandling behandlingSpy = spy(behandling);
        when(behandlingSpy.getId()).thenReturn(behandlingId);

        List<BekreftetAksjonspunktDto> aksjonspunkterDtoer = Collections.singletonList(new VurderForeldelseDto());
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingSpy.getId(), 2L, aksjonspunkterDtoer);

        when(behandlingRepositoryMock.erVersjonUendret(anyLong(), anyLong())).thenReturn(true);

        aksjonspunktRestTjeneste.bekreft(dto);

        verify(aksjonspunktTjenesteMock, atLeastOnce()).bekreftAksjonspunkter(aksjonspunkterDtoer, behandlingId);
    }

}
