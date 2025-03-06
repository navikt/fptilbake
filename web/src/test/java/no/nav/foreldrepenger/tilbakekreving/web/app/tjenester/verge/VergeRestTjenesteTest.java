package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.UuidDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge.dto.NyVergeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(JpaExtension.class)
class VergeRestTjenesteTest {

    private final VergeTjeneste vergeTjenesteMock = mock(VergeTjeneste.class);
    private final BehandlingTjeneste behandlingTjenesteMock = mock(BehandlingTjeneste.class);
    private final PersoninfoAdapter tpsTjenesteMock = mock(PersoninfoAdapter.class);
    private final VergeRestTjeneste vergeRestTjeneste = new VergeRestTjeneste(behandlingTjenesteMock, vergeTjenesteMock, tpsTjenesteMock);
    private BehandlingRepositoryProvider repositoryProvider;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
    }

    @Nested
    class OppretteVerge {

        @Test
        void skal_opprette_verge() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
            var uuidDto = new UuidDto(behandling.getUuid());
            var body = new NyVergeDto("John Doe", "12345678901", LocalDate.now().minusMonths(1), LocalDate.now(), VergeType.BARN, null);

            var response = vergeRestTjeneste.opprettVerge(uuidDto, body);
            verify(vergeTjenesteMock, atLeastOnce()).opprettVerge(any(), any());
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        }
    }

    @Nested
    class FjerneVerge {

        @Test
        void skal_fjerne_verge() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
            var uuidDto = new UuidDto(behandling.getUuid());
            var response = vergeRestTjeneste.fjernVerge(uuidDto);
            verify(vergeTjenesteMock, atLeastOnce()).fjernVerge(any());
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);

        }
    }

    @Nested
    class HentVerge {
        @Test
        void skal_hente_verge_som_finnes() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            Behandling behandling = scenario.lagre(repositoryProvider);
            var vergeEntitet = VergeEntitet.builder().medVergeAktørId(behandling.getAktørId())
                    .medVergeType(VergeType.BARN)
                    .medKilde(KildeType.FPTILBAKE.name())
                    .medNavn("John Doe")
                    .medGyldigPeriode(LocalDate.now().minusMonths(1), LocalDate.now())
                    .medBegrunnelse("begunnlese").build();
            when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);
            when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.of(vergeEntitet));

            var uuidDto = new UuidDto(behandling.getUuid());
            assertThat(vergeRestTjeneste.hentVerge(uuidDto)).isNotNull();
            verify(vergeTjenesteMock, atLeastOnce()).hentVergeInformasjon(any());
        }

        @Test
        void skal_hente_verge_som_ikke_finnes() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(vergeTjenesteMock.hentVergeInformasjon(anyLong())).thenReturn(Optional.empty());
            when(behandlingTjenesteMock.hentBehandling(any(UUID.class))).thenReturn(behandling);

            var uuidDto = new UuidDto(behandling.getUuid());
            assertThat(vergeRestTjeneste.hentVerge(uuidDto)).isNull();
            verify(vergeTjenesteMock, atLeastOnce()).hentVergeInformasjon(any());
        }
    }

    @Nested
    class OppretteVergeMedAP {

        @Test
        void kan_ikke_opprette_verge_når_behandling_er_avsluttet() {
            Behandling behandling = ScenarioSimple.simple().lagMocked();
            behandling.avsluttBehandling();
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            BehandlingReferanse behandlingReferanse = new BehandlingReferanse(behandling.getId());
            var e = assertThrows(TekniskException.class, () -> vergeRestTjeneste.opprettVerge(mock(HttpServletRequest.class), behandlingReferanse));
            assertThat(e.getMessage()).contains("FPT-763493");
        }

        @Test
        void kan_ikke_opprette_verge_når_behandling_er_på_vent() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            BehandlingReferanse behandlingReferanse = new BehandlingReferanse(behandling.getId());
            var e = assertThrows(TekniskException.class, () -> vergeRestTjeneste.opprettVerge(mock(HttpServletRequest.class), behandlingReferanse));
            assertThat(e.getMessage()).contains("FPT-763493");
        }

        @Test
        void kan_ikke_opprette_verge_når_behandling_har_allerede_verge_aksjonspunkt() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_FEILUTBETALING);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            BehandlingReferanse behandlingReferanse = new BehandlingReferanse(behandling.getId());
            var e = assertThrows(TekniskException.class, () -> vergeRestTjeneste.opprettVerge(mock(HttpServletRequest.class), behandlingReferanse));
            assertThat(e.getMessage()).contains("FPT-185321");
        }

        @Test
        void skal_opprette_verge_når_behandling_er_i_fakta_steg() throws URISyntaxException {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            vergeRestTjeneste.opprettVerge(mock(HttpServletRequest.class), new BehandlingReferanse(behandling.getId()));
            verify(vergeTjenesteMock, atLeastOnce()).opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(any());
        }
    }

    @Nested
    class FjerneVergeMedAP {
        @Test
        void kan_ikke_fjerne_verge_når_behandling_er_avsluttet() {
            Behandling behandling = ScenarioSimple.simple().lagMocked();
            behandling.avsluttBehandling();
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            BehandlingReferanse behandlingReferanse = new BehandlingReferanse(behandling.getId());
            var e = assertThrows(TekniskException.class, () -> vergeRestTjeneste.fjernVerge(mock(HttpServletRequest.class), behandlingReferanse));
            assertThat(e.getMessage()).contains("FPT-763494");
        }

        @Test
        void kan_ikke_fjerne_verge_når_behandling_er_på_vent() {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            BehandlingReferanse behandlingReferanse = new BehandlingReferanse(behandling.getId());
            var e = assertThrows(TekniskException.class, () -> vergeRestTjeneste.fjernVerge(mock(HttpServletRequest.class), behandlingReferanse));
            assertThat(e.getMessage()).contains("FPT-763494");
        }

        @Test
        void skal_fjerne_verge() throws URISyntaxException {
            ScenarioSimple scenario = ScenarioSimple.simple();
            scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.FAKTA_VERGE);
            Behandling behandling = scenario.lagre(repositoryProvider);
            when(behandlingTjenesteMock.hentBehandling(anyLong())).thenReturn(behandling);
            vergeRestTjeneste.fjernVerge(mock(HttpServletRequest.class), new BehandlingReferanse(behandling.getId()));
            verify(vergeTjenesteMock, atLeastOnce()).fjernVergeGrunnlagOgAksjonspunkt(any());
        }
    }
}
