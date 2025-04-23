package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingHistorikkTjeneste;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsak.FagsakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.FatteVedtakDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.VurderForeldelseDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

class AksjonspunktRestTjenesteTest {

    private static final Saksnummer SAKSNUMMER = new Saksnummer("12345");
    private static final NavBruker NAV_BRUKER = NavBruker.opprettNy(new AktørId(12345L), Språkkode.NB);

    private final AksjonspunktApplikasjonTjeneste aksjonspunktTjenesteMock = mock(AksjonspunktApplikasjonTjeneste.class);

    private final BehandlingRepository behandlingRepositoryMock = mock(BehandlingRepository.class);
    private final TotrinnRepository totrinnRepositoryMock = mock(TotrinnRepository.class);
    private final BehandlingRepositoryProvider repositoryProviderMock = mock(BehandlingRepositoryProvider.class);
    private final ProsessTaskTjeneste taskTjenesteMock = mock(ProsessTaskTjeneste.class);

    private final BehandlingskontrollTjeneste behandlingskontrollTjenesteMock = mock(BehandlingskontrollTjeneste.class);
    private final BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjenesteMock = mock(BehandlingskontrollAsynkTjeneste.class);
    private final BehandlingskontrollProvider behandlingskontrollProvider = new BehandlingskontrollProvider(behandlingskontrollTjenesteMock, behandlingskontrollAsynkTjenesteMock);

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    private Behandling behandling;

    @BeforeEach
    void setup() {
        when(repositoryProviderMock.getBehandlingRepository()).thenReturn(behandlingRepositoryMock);

        BehandlingTjeneste behandlingTjeneste = new BehandlingTjeneste(repositoryProviderMock,
                behandlingskontrollProvider,
                mock(FagsakTjeneste.class),
                mock(BehandlingHistorikkTjeneste.class),
                mock(FagsystemKlient.class));

        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(behandlingRepositoryMock,
                totrinnRepositoryMock,
                behandlingTjeneste,
                aksjonspunktTjenesteMock);

        Fagsak fagsak = Fagsak.opprettNy(SAKSNUMMER, NAV_BRUKER);
        behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).build();
        behandling.setId(1234L);
    }

    @Test
    void test_skal_hente_aksjonspunkter() {
        when(behandlingRepositoryMock.hentBehandling(anyLong())).thenReturn(behandling);

        Totrinnsvurdering ttv = Totrinnsvurdering.builder()
                .medBehandling(behandling)
                .medAksjonspunktDefinisjon(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING).build();
        when(totrinnRepositoryMock.hentTotrinnsvurderinger(any(Behandling.class))).thenReturn(Collections.singleton(ttv));

        Response result = aksjonspunktRestTjeneste.getAksjonspunkter(new BehandlingReferanse("1234"));

        assertThat(result.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(result.getEntity()).isNotNull();
    }

    @Test
    void test_skal_kalle_bekreftAksjonspunktert() throws URISyntaxException {
        long behandlingId = 12345L;
        Behandling behandlingSpy = spy(behandling);
        when(behandlingSpy.getId()).thenReturn(behandlingId);
        when(behandlingSpy.getUuid()).thenReturn(UUID.randomUUID());

        List<BekreftetAksjonspunktDto> aksjonspunkterDtoer = Collections.singletonList(new VurderForeldelseDto());
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingSpy.getId(), 2L, aksjonspunkterDtoer);

        when(behandlingRepositoryMock.hentBehandling(anyLong())).thenReturn(behandlingSpy);
        when(behandlingRepositoryMock.erVersjonUendret(anyLong(), anyLong())).thenReturn(true);

        aksjonspunktRestTjeneste.bekreft(mock(HttpServletRequest.class), dto);

        verify(aksjonspunktTjenesteMock, atLeastOnce()).bekreftAksjonspunkter(aksjonspunkterDtoer, behandlingId);
    }

    @Test
    void skal_kunne_sende_fatte_vedtak_til_beslutter_endepunkt() throws URISyntaxException {
        when(behandlingRepositoryMock.erVersjonUendret(anyLong(), anyLong())).thenReturn(true);
        when(behandlingRepositoryMock.hentBehandling(anyLong())).thenReturn(behandling);
        aksjonspunktRestTjeneste.beslutt(mock(HttpServletRequest.class), BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(),
            List.of(new FatteVedtakDto(List.of(new AksjonspunktGodkjenningDto())))));

        verify(aksjonspunktTjenesteMock).bekreftAksjonspunkter(ArgumentMatchers.anyCollection(), anyLong());
    }

    @Test
    void skal_ikke_kunne_sende_andre_ap_til_beslutter_endepunkt() {
        var dto = BekreftedeAksjonspunkterDto.lagDto(1L, 1L,
            List.of(new VurderForeldelseDto()));
        assertThatThrownBy(() -> aksjonspunktRestTjeneste.beslutt(mock(HttpServletRequest.class),
            dto)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void skal_ikke_kunne_sende_fatter_vedtak_ap_til_aksjonspunkt_endepunkt() {
        var dto = BekreftedeAksjonspunkterDto.lagDto(1L, 1L, List.of(new FatteVedtakDto(List.of())));
        assertThatThrownBy(() -> aksjonspunktRestTjeneste.bekreft(mock(HttpServletRequest.class), dto)).isExactlyInstanceOf(
            IllegalArgumentException.class);
    }

}
