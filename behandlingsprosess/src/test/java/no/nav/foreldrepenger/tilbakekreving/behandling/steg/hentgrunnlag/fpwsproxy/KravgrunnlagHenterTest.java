package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.AnnullerKravGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.sokos.HentKravgrunnlagMapperSokos;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerRequest;
import no.nav.foreldrepenger.tilbakekreving.sokos.HentKravgrunnlagDetaljerResponse;
import no.nav.foreldrepenger.tilbakekreving.sokos.KravgrunnlagAnnulerRequest;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosTilbakekrevingKlient;
import no.nav.vedtak.exception.IntegrasjonException;

class KravgrunnlagHenterTest {

    private final ØkonomiProxyKlient proxy = mock(ØkonomiProxyKlient.class);
    private final HentKravgrunnlagMapperProxy proxyMapper = mock(HentKravgrunnlagMapperProxy.class);
    private final SokosTilbakekrevingKlient sokos = mock(SokosTilbakekrevingKlient.class);
    private final HentKravgrunnlagMapperSokos sokosMapper = mock(HentKravgrunnlagMapperSokos.class);

    @BeforeEach
    void setup() {
        System.setProperty("app.name", "fptilbake");
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("app.name");
    }

    @Test
    void annullerer_via_sokos_i_dev_med_fast_enhet_og_saksbehandler() {
        var behandlingId = 123L;
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(13434L);
        when(grunnlag.getAnsvarligEnhet()).thenReturn("4819");

        henter(true).annullerKravgrunnlag(behandlingId, grunnlag);

        verify(sokos).anullerKravgrunnlag(new KravgrunnlagAnnulerRequest(
            KodeAksjon.ANNULERE_GRUNNLAG, 13434,
            KravgrunnlagHenter.ANSVARLIG_ENHET_NØS, KravgrunnlagHenter.OKO_SAKSBEH_ID));
        verify(proxy, never()).anullerKravgrunnlag(any());
    }

    @Test
    void annullerer_via_proxy_utenfor_dev() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(13434L);

        henter(false).annullerKravgrunnlag(123L, grunnlag);

        verify(proxy).anullerKravgrunnlag(new AnnullerKravGrunnlagDto(BigInteger.valueOf(13434L)));
        verify(sokos, never()).anullerKravgrunnlag(any());
    }

    @Test
    void avviser_vedtakid_over_grensen_for_sokos() {
        var grunnlag = mock(Kravgrunnlag431.class);
        when(grunnlag.getVedtakId()).thenReturn(1_000_000_000L);

        assertThatThrownBy(() -> henter(true).annullerKravgrunnlag(123L, grunnlag))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("vedtakId");
        verify(sokos, never()).anullerKravgrunnlag(any());
    }

    @Test
    void avviser_annullering_uten_aktivt_kravgrunnlag() {
        assertThatThrownBy(() -> henter(true).annullerKravgrunnlag(123L, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Finnes ikke aktivt kravgrunnlag");
        verify(sokos, never()).anullerKravgrunnlag(any());
        verify(proxy, never()).anullerKravgrunnlag(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"4", "5"})
    void bruker_sokos_med_opprinnelig_aksjon_og_identifikatorer_i_dev(String aksjon) {
        var ka = "4".equals(aksjon) ? KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG : KodeAksjon.HENT_GRUNNLAG_OMGJØRING;
        var request = request(ka, BigInteger.valueOf(570598));
        var sokosRequest = new HentKravgrunnlagDetaljerRequest(ka, 570598, "4819", "Z123456");
        var respons = mock(HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer.class);
        var resultat = mock(Kravgrunnlag431.class);
        when(sokos.hentKravgrunnlag(sokosRequest)).thenReturn(new HentKravgrunnlagDetaljerResponse(0, "OK", respons));
        when(sokosMapper.mapTilDomene(respons)).thenReturn(resultat);

        assertThat(henter(true).hentKravgrunnlagFraOS(1L, request)).isSameAs(resultat);

        verify(proxy, never()).hentKravgrunnlag(any());
    }

    @Test
    void beholder_proxy_utenfor_dev() {
        var request = request(KodeAksjon.HENT_GRUNNLAG_OMGJØRING, BigInteger.valueOf(570598));
        var respons = mock(Kravgrunnlag431Dto.class);
        var resultat = mock(Kravgrunnlag431.class);
        when(proxy.hentKravgrunnlag(any())).thenReturn(respons);
        when(proxyMapper.mapTilDomene(respons)).thenReturn(resultat);

        assertThat(henter(false).hentKravgrunnlagFraOS(null, request)).isSameAs(resultat);

        verify(sokos, never()).hentKravgrunnlag(any());
    }

    @Test
    void avviser_kravgrunnlagid_over_sokos_grensen() {
        assertThatThrownBy(() -> henter(true).hentKravgrunnlagFraOS(null, request(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG, BigInteger.valueOf(1_000_000_000))))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("kravgrunnlagId");
        verify(sokos, never()).hentKravgrunnlag(any());
    }

    @Test
    void avviser_kravgrunnlagid_som_ikke_er_int() {
        assertThatThrownBy(() -> henter(true).hentKravgrunnlagFraOS(null, request(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG, new BigInteger("9223372036854775808"))))
            .isInstanceOf(ArithmeticException.class);
        verify(sokos, never()).hentKravgrunnlag(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void godtar_detaljer_og_returnerer_grunnlag(int status) {
        var detaljer = mock(HentKravgrunnlagDetaljerResponse.KravgrunnlagDetaljer.class);
        assertThat(KravgrunnlagHenter.validerDetaljer(new HentKravgrunnlagDetaljerResponse(status, "OK", detaljer)))
            .isSameAs(detaljer);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 12, 16})
    void avviser_feilstatus_ved_henting(int status) {
        assertThatThrownBy(() -> KravgrunnlagHenter.validerDetaljer(new HentKravgrunnlagDetaljerResponse(status, "Feil", null)))
            .isInstanceOf(IntegrasjonException.class)
            .hasMessageContaining("status " + status);
    }

    @Test
    void avviser_manglende_grunnlag_eller_status() {
        assertThatThrownBy(() -> KravgrunnlagHenter.validerDetaljer(new HentKravgrunnlagDetaljerResponse(0, "OK", null)))
            .isInstanceOf(IntegrasjonException.class);
        assertThatThrownBy(() -> KravgrunnlagHenter.validerDetaljer(new HentKravgrunnlagDetaljerResponse(null, "OK", null)))
            .isInstanceOf(IntegrasjonException.class);
    }

    @Test
    void beholder_spesialhåndtering_for_manglende_og_sperret_grunnlag() {
        assertThatThrownBy(() -> KravgrunnlagHenter.validerFeilkoder("B420010I"))
            .isInstanceOf(ManglendeKravgrunnlagException.class);
        assertThatThrownBy(() -> KravgrunnlagHenter.validerDetaljer(new HentKravgrunnlagDetaljerResponse(4, "B420012I", null)))
            .isInstanceOf(SperringKravgrunnlagException.class);
        when(sokos.hentKravgrunnlag(any())).thenThrow(new SokosKravgrunnlagException("B420010I"));
        assertThatThrownBy(() -> henter(true).hentKravgrunnlagFraOS(null, request(KodeAksjon.HENT_GRUNNLAG_OMGJØRING, BigInteger.valueOf(570598))))
            .isInstanceOf(ManglendeKravgrunnlagException.class);
    }

    private KravgrunnlagHenter henter(boolean dev) {
        return new KravgrunnlagHenter(proxy, proxyMapper, sokos, sokosMapper, () -> dev);
    }

    private static HentKravgrunnlagRequest request(KodeAksjon aksjon, BigInteger id) {
        return new HentKravgrunnlagRequest.Builder()
            .kodeAksjon(aksjon)
            .kravgrunnlagId(id)
            .enhetAnsvarlig("4819")
            .saksbehId("Z123456")
            .build();
    }
}
